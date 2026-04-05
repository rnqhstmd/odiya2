# 설계: 프로필 이미지 변경 + 회원 탈퇴 화면 전환 + QR 친구 추가

## 설계 규모
중형

## 현재 상태 분석

### 네비게이션 계층
```
OdiyaApp → RootRouterView (@StateObject RootRouter)
  → .environmentObject(router)
  → MainTabView (@EnvironmentObject RootRouter)
    → .sheet(ProfileMenuView) — onLogout 콜백으로 pendingLogout 플래그 세팅
      → NavigationLink → ProfileView — ⚠️ onLogout 미주입
      → NavigationLink → SettingsView — @EnvironmentObject RootRouter 정상 사용
```

### 핵심 발견 사항
1. **ProfileView의 onLogout 미주입 문제**: `ProfileMenuView`에서 `ProfileView()`를 NavigationLink로 push하지만, `ProfileViewModel.onLogout` 콜백을 주입하지 않는다. 따라서 `ProfileView`에서 로그아웃/탈퇴 후 로그인 화면 전환이 불가능하다.
2. **SettingsView의 해결 패턴**: `SettingsView`는 `@EnvironmentObject private var router: RootRouter`를 선언하고, `.onAppear`에서 `viewModel.onLogout = { router.navigateToLogin() }`으로 주입한다. 이 패턴을 ProfileView에도 동일 적용하면 된다.
3. **기존 updateProfileImage API**: 백엔드에 `PATCH /api/v1/users/me/profile-image` 엔드포인트가 이미 존재한다. iOS에도 `UserUseCase.updateProfileImage(_:)`, `UserRepository.updateProfileImage(_:)`, `UserRepositoryImpl.updateProfileImage(_:)`, `APIEndpoint.updateProfileImage`가 모두 구현되어 있다. 다만 **Presigned URL 발급 API**와 **iOS에서 S3 직접 업로드 로직**이 없다.
4. **QR 코드 UI 스텁**: `AddFriendView`에 QR 섹션 UI가 이미 있고, "스캔하기"/"내 QR" 버튼에 `// TODO` 주석만 남아 있다.
5. **AWS SDK 부재**: 백엔드에 AWS SDK 의존성이 없다. Presigned URL 발급을 위해 `software.amazon.awssdk:s3` 의존성 추가가 필요하다.

---

## 변경 범위

### 신규 파일

#### 백엔드
| 파일 | 역할 |
|------|------|
| `backend/modules/storage/build.gradle.kts` | S3 스토리지 모듈 빌드 설정 (AWS S3 SDK 의존성) |
| `backend/modules/storage/src/main/java/com/loopers/storage/StorageConfig.java` | S3Client Bean 설정 (@Configuration) |
| `backend/modules/storage/src/main/java/com/loopers/storage/StorageService.java` | Presigned URL 생성 로직 (S3Presigner) |
| `backend/modules/storage/src/main/resources/storage.yml` | S3 버킷명, 리전, presigned URL 만료시간 설정 |
| `backend/apps/odiya-api/src/main/java/com/loopers/interfaces/api/storage/StorageV1Controller.java` | `POST /api/v1/storage/presigned-url` 엔드포인트 |
| `backend/apps/odiya-api/src/main/java/com/loopers/interfaces/api/storage/StorageV1Dto.java` | Presigned URL 요청/응답 DTO |
| `backend/apps/odiya-api/src/main/java/com/loopers/interfaces/api/storage/StorageV1ApiSpec.java` | Swagger API 명세 인터페이스 |

#### iOS
| 파일 | 역할 |
|------|------|
| `ios/Odiya/Core/Network/ImageUploadService.swift` | Presigned URL 요청 + S3 PUT 업로드 로직 |
| `ios/Odiya/Features/Friend/Presentation/QR/QRCodeView.swift` | 내 QR 코드 표시 화면 (CIQRCodeGenerator) |
| `ios/Odiya/Features/Friend/Presentation/QR/QRScannerView.swift` | QR 스캔 화면 (AVCaptureSession, UIViewRepresentable) |
| `ios/Odiya/Features/Friend/Presentation/QR/QRScannerViewModel.swift` | QR 스캔 결과 파싱 + 친구 요청 전송 ViewModel |

### 수정 파일

#### 백엔드
| 파일 | 변경 내용 |
|------|-----------|
| `backend/settings.gradle.kts` | `":modules:storage"` 모듈 include 추가 |
| `backend/apps/odiya-api/build.gradle.kts` | `implementation(project(":modules:storage"))` 의존성 추가 |

#### iOS
| 파일 | 변경 내용 |
|------|-----------|
| `ios/Odiya/Features/User/Presentation/ProfileView.swift` | (1) `@EnvironmentObject RootRouter` 추가 (2) PhotosPicker 추가 (3) onLogout 콜백 주입 |
| `ios/Odiya/Features/User/Presentation/ProfileViewModel.swift` | `uploadProfileImage(imageData:)` 메서드 추가 |
| `ios/Odiya/Features/User/Domain/UserUseCase.swift` | `uploadAndUpdateProfileImage(imageData:)` 메서드 추가 (protocol + 구현) |
| `ios/Odiya/Features/User/Domain/UserRepository.swift` | `getPresignedURL(fileName:contentType:)` 메서드 추가 |
| `ios/Odiya/Features/User/Data/UserRepositoryImpl.swift` | `getPresignedURL` 구현 추가 |
| `ios/Odiya/Features/User/Data/UserDTO.swift` | `PresignedURLRequest`, `PresignedURLResponse` DTO 추가 |
| `ios/Odiya/Core/Network/APIEndpoint.swift` | `getPresignedURL` case 추가 |
| `ios/Odiya/Features/Friend/Presentation/AddFriendView.swift` | QR 버튼 액션에 sheet 네비게이션 연결 |
| `ios/Odiya/Features/Friend/Presentation/AddFriendViewModel.swift` | `loadMyUserId()`, QR 스캔 결과로 친구 요청 전송 메서드 추가 |
| `ios/Odiya/Resources/Info.plist` | `NSCameraUsageDescription` 키 추가 (QR 스캔용) |

---

## 구현 순서

### 단계 1: 백엔드 — S3 Presigned URL 모듈 및 API [병렬 불가 — 순차]

#### 1-A: storage 모듈 생성

**`backend/modules/storage/build.gradle.kts`** (신규)
```kotlin
dependencies {
    implementation("software.amazon.awssdk:s3:2.29.51")
    implementation("software.amazon.awssdk:sts:2.29.51")  // credential provider
}
```

**`backend/modules/storage/src/main/resources/storage.yml`** (신규)
```yaml
cloud:
  aws:
    s3:
      bucket: ${S3_BUCKET_NAME}
      region: ${AWS_REGION:ap-northeast-2}
      presigned-url-expiration: 600  # 초
    credentials:
      access-key: ${AWS_ACCESS_KEY}
      secret-key: ${AWS_SECRET_KEY}
```

**`backend/modules/storage/src/main/java/com/loopers/storage/StorageConfig.java`** (신규)
```java
@Configuration
@ConfigurationProperties(prefix = "cloud.aws.s3")
public class StorageConfig {
    private String bucket;
    private String region;
    private int presignedUrlExpiration;
    // getter/setter

    @Bean
    public S3Presigner s3Presigner() { ... }
}
```

**`backend/modules/storage/src/main/java/com/loopers/storage/StorageService.java`** (신규)
```java
@Component
@RequiredArgsConstructor
public class StorageService {
    private final S3Presigner s3Presigner;
    private final StorageConfig config;

    public PresignedUrlResult generatePresignedUrl(String fileName, String contentType) {
        String objectKey = "profile-images/" + UUID.randomUUID() + "/" + fileName;
        // PutObjectRequest → PutObjectPresignRequest → s3Presigner.presignPutObject()
        // return { presignedUrl, objectKey, publicUrl }
    }
}
```

#### 1-B: API 엔드포인트 추가

**`backend/settings.gradle.kts`** — include에 `":modules:storage"` 추가

**`backend/apps/odiya-api/build.gradle.kts`** — `implementation(project(":modules:storage"))` 추가

**`backend/apps/odiya-api/.../storage/StorageV1Dto.java`** (신규)
```java
public class StorageV1Dto {
    public record PresignedUrlRequest(
        @NotBlank String fileName,
        @NotBlank @Pattern(regexp = "^image/(jpeg|png|webp|heic)$") String contentType
    ) {}

    public record PresignedUrlResponse(
        String presignedUrl,   // PUT 요청할 URL
        String imageUrl        // 업로드 완료 후 접근 가능한 public URL
    ) {}
}
```

**`backend/apps/odiya-api/.../storage/StorageV1Controller.java`** (신규)
```java
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/storage")
public class StorageV1Controller implements StorageV1ApiSpec {
    private final StorageService storageService;

    @PostMapping("/presigned-url")
    public ApiResponse<StorageV1Dto.PresignedUrlResponse> getPresignedUrl(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody StorageV1Dto.PresignedUrlRequest request
    ) { ... }
}
```

기존 패턴 준수:
- Controller → 직접 StorageService 호출 (단순 URL 발급이므로 Facade 불필요)
- `ApiResponse<T>` 래핑 응답
- `@AuthenticationPrincipal LoginUser` 인증
- `UserV1ApiSpec` 패턴과 동일한 Swagger `StorageV1ApiSpec` 인터페이스

---

### 단계 2: iOS — Presigned URL 업로드 인프라 [병렬 가능: 2-A, 2-B 동시]

#### 2-A: API 엔드포인트 + DTO 추가

**`ios/Odiya/Core/Network/APIEndpoint.swift`** 수정:
- `enum APIEndpoint`에 `case getPresignedURL` 추가
- `path`: `"/api/v1/storage/presigned-url"`
- `method`: `.post`
- `requiresAuth`: `true` (default)

**`ios/Odiya/Features/User/Data/UserDTO.swift`** 수정:
```swift
// 기존 코드 아래에 추가
/// POST /api/v1/storage/presigned-url
struct PresignedURLRequest: Encodable {
    let fileName: String
    let contentType: String
}

struct PresignedURLResponse: Decodable {
    let presignedUrl: String
    let imageUrl: String
}
```

#### 2-B: ImageUploadService 생성

**`ios/Odiya/Core/Network/ImageUploadService.swift`** (신규):
```swift
import Foundation

actor ImageUploadService {
    static let shared = ImageUploadService()
    private let session: URLSession

    init(session: URLSession = .shared) {
        self.session = session
    }

    /// Presigned URL로 이미지 데이터를 S3에 PUT 업로드
    func upload(imageData: Data, to presignedUrl: String, contentType: String) async throws {
        guard let url = URL(string: presignedUrl) else {
            throw APIError.unknown("잘못된 업로드 URL입니다.")
        }
        var request = URLRequest(url: url)
        request.httpMethod = "PUT"
        request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = imageData

        let (_, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse,
              (200...299).contains(httpResponse.statusCode) else {
            throw APIError.unknown("이미지 업로드에 실패했습니다.")
        }
    }
}
```

APIClient 패턴(actor, static shared, URLSession 주입)과 일관된 구조.

---

### 단계 3: iOS — 프로필 이미지 변경 + 회원 탈퇴 화면 전환 [병렬 가능: 3-A, 3-B 동시]

#### 3-A: 프로필 이미지 변경

**`ios/Odiya/Features/User/Domain/UserRepository.swift`** 수정:
```swift
// protocol에 추가
func getPresignedURL(fileName: String, contentType: String) async throws -> PresignedURLResponse
```

**`ios/Odiya/Features/User/Data/UserRepositoryImpl.swift`** 수정:
```swift
// 기존 메서드들 아래에 추가
func getPresignedURL(fileName: String, contentType: String) async throws -> PresignedURLResponse {
    let body = PresignedURLRequest(fileName: fileName, contentType: contentType)
    return try await apiClient.request(
        endpoint: .getPresignedURL,
        body: body,
        responseType: PresignedURLResponse.self
    )
}
```

**`ios/Odiya/Features/User/Domain/UserUseCase.swift`** 수정:
```swift
// protocol에 추가
func uploadAndUpdateProfileImage(_ imageData: Data) async throws -> User

// 구현에 추가
func uploadAndUpdateProfileImage(_ imageData: Data) async throws -> User {
    let fileName = "profile_\(UUID().uuidString).jpg"
    let contentType = "image/jpeg"

    // 1. Presigned URL 발급
    let presigned = try await userRepository.getPresignedURL(
        fileName: fileName, contentType: contentType
    )

    // 2. S3에 이미지 업로드
    try await ImageUploadService.shared.upload(
        imageData: imageData, to: presigned.presignedUrl, contentType: contentType
    )

    // 3. 프로필 이미지 URL 업데이트
    let dto = try await userRepository.updateProfileImage(presigned.imageUrl)
    return dto.toDomain()
}
```

**`ios/Odiya/Features/User/Presentation/ProfileViewModel.swift`** 수정:
```swift
// Published 프로퍼티 추가
@Published var selectedImageData: Data?

// 메서드 추가
func uploadProfileImage(_ imageData: Data) {
    isLoading = true
    Task {
        do {
            user = try await userUseCase.uploadAndUpdateProfileImage(imageData)
        } catch let error as APIError {
            errorMessage = error.userMessage
        } catch {
            errorMessage = "프로필 이미지 변경에 실패했습니다."
        }
        isLoading = false
    }
}
```

**`ios/Odiya/Features/User/Presentation/ProfileView.swift`** 수정:
```swift
import SwiftUI
import PhotosUI  // 추가

struct ProfileView: View {
    @StateObject private var viewModel = ProfileViewModel()
    @EnvironmentObject private var router: RootRouter  // 추가
    @State private var editingNickname = ""
    @State private var isEditingNickname = false
    @State private var selectedPhotoItem: PhotosPickerItem?  // 추가

    // 프로필 이미지 영역을 PhotosPicker로 래핑
    // 기존:
    //   AsyncImage(url: ...) { ... }
    //     .frame(width: 64, height: 64)
    //     .clipShape(Circle())
    // 변경:
    //   PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
    //       <기존 AsyncImage 코드>
    //       .overlay(alignment: .bottomTrailing) {
    //           Image(systemName: "camera.circle.fill")
    //               .font(.system(size: 22))
    //               .foregroundStyle(OdiyaColors.primary)
    //               .background(Circle().fill(.white))
    //       }
    //   }
    //   .buttonStyle(.plain)
    //   .onChange(of: selectedPhotoItem) { _, newItem in
    //       Task {
    //           if let data = try? await newItem?.loadTransferable(type: Data.self) {
    //               viewModel.uploadProfileImage(data)
    //           }
    //       }
    //   }
}
```

#### 3-B: 회원 탈퇴 후 로그인 화면 전환

**`ios/Odiya/Features/User/Presentation/ProfileView.swift`** 수정 (3-A와 같은 파일이므로 병합):

SettingsView 패턴과 동일하게:
```swift
// 기존 .task { } 블록 아래에 추가
.onAppear {
    viewModel.onLogout = { router.navigateToLogin() }
}
```

이로써 `ProfileViewModel.logout()`과 `ProfileViewModel.deleteAccount()` 내부의 `onLogout?()` 호출이 `router.navigateToLogin()`을 트리거하게 된다.

> **주의**: `ProfileView`는 `ProfileMenuView` 내부 `NavigationLink`로 push된다. `ProfileMenuView`는 `MainTabView`의 `.sheet()`으로 표시되고, `MainTabView`에 `.environmentObject(router)`가 주입되어 있으므로, `ProfileView`에서도 `@EnvironmentObject RootRouter`를 정상적으로 접근할 수 있다.

---

### 단계 4: iOS — QR 코드 친구 추가 [병렬 가능: 4-A, 4-B, 4-C 동시]

#### 4-A: 내 QR 코드 표시

**`ios/Odiya/Features/Friend/Presentation/QR/QRCodeView.swift`** (신규):
```swift
import SwiftUI
import CoreImage.CIFilterBuiltins

struct QRCodeView: View {
    let userId: Int64
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Text("내 QR 코드")
                    .font(.title2).fontWeight(.bold)

                // CIQRCodeGenerator로 "odiya://friend?userId=\(userId)" 인코딩
                if let qrImage = generateQRCode(from: "odiya://friend?userId=\(userId)") {
                    Image(uiImage: qrImage)
                        .interpolation(.none)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 220, height: 220)
                        .padding(20)
                        .background(Color.white)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .shadow(color: .black.opacity(0.1), radius: 8)
                }

                Text("친구가 이 QR 코드를 스캔하면\n친구 요청이 전송됩니다")
                    .font(.subheadline).foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("닫기") { dismiss() }
                }
            }
        }
    }

    private func generateQRCode(from string: String) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(string.utf8)
        filter.correctionLevel = "M"
        guard let ciImage = filter.outputImage else { return nil }
        let transform = CGAffineTransform(scaleX: 10, y: 10)
        let scaledImage = ciImage.transformed(by: transform)
        return UIImage(ciImage: scaledImage)
    }
}
```

QR 데이터 형식: `odiya://friend?userId={userId}` -- 앱 내부용 딥링크 스킴.

userId 획득: 기존 `GET /api/v1/users/me` API (`APIEndpoint.getMyProfile`)를 통해 로드. AddFriendViewModel에서 이미 사용 가능한 패턴.

#### 4-B: QR 스캔 화면

**`ios/Odiya/Features/Friend/Presentation/QR/QRScannerView.swift`** (신규):
```swift
import SwiftUI
import AVFoundation

struct QRScannerView: View {
    @StateObject private var viewModel = QRScannerViewModel()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ZStack {
                CameraPreview(session: viewModel.captureSession)
                    .ignoresSafeArea()

                // 스캔 가이드 오버레이 (반투명 + 중앙 사각형 투명)
                // ...
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("닫기") { dismiss() }
                }
            }
            .task { await viewModel.startScanning() }
            .onDisappear { viewModel.stopScanning() }
            .alert("알림", isPresented: $viewModel.showAlert) {
                Button("확인") { dismiss() }
            } message: {
                Text(viewModel.alertMessage)
            }
        }
    }
}

// AVCaptureVideoPreviewLayer를 UIViewRepresentable로 래핑
struct CameraPreview: UIViewRepresentable {
    let session: AVCaptureSession

    func makeUIView(context: Context) -> UIView { ... }
    func updateUIView(_ uiView: UIView, context: Context) {}
}
```

#### 4-C: QR 스캔 ViewModel + AddFriendView/ViewModel 수정

**`ios/Odiya/Features/Friend/Presentation/QR/QRScannerViewModel.swift`** (신규):
```swift
@MainActor
final class QRScannerViewModel: NSObject, ObservableObject {
    @Published var showAlert = false
    @Published var alertMessage = ""

    let captureSession = AVCaptureSession()
    private let repository: FriendRepository

    init(repository: FriendRepository = FriendRepositoryImpl()) {
        self.repository = repository
    }

    func startScanning() async { ... }
    func stopScanning() { ... }

    // AVCaptureMetadataOutputObjectsDelegate에서 QR 스트링 파싱
    // "odiya://friend?userId=\(id)" → userId 추출 → repository.sendFriendRequest(targetUserId:)
}
extension QRScannerViewModel: AVCaptureMetadataOutputObjectsDelegate { ... }
```

**`ios/Odiya/Features/Friend/Presentation/AddFriendView.swift`** 수정:
```swift
// State 추가
@State private var showQRScanner = false
@State private var showMyQR = false

// 기존 TODO 버튼 액션 교체:
Button {
    showQRScanner = true      // 기존: // TODO: QR 스캔 화면 이동
} label: { Text("스캔하기") ... }

Button {
    showMyQR = true           // 기존: // TODO: 내 QR 코드 표시 화면 이동
} label: { Text("내 QR") ... }

// sheet modifier 추가 (NavigationStack 바깥 또는 List 아래)
.sheet(isPresented: $showQRScanner) {
    QRScannerView()
}
.sheet(isPresented: $showMyQR) {
    if let userId = viewModel.myUserId {
        QRCodeView(userId: userId)
    }
}
```

**`ios/Odiya/Features/Friend/Presentation/AddFriendViewModel.swift`** 수정:
```swift
// Published 추가
@Published var myUserId: Int64?

// Dependencies 추가
private let userRepository: UserRepository

init(
    repository: FriendRepository = FriendRepositoryImpl(),
    userRepository: UserRepository = UserRepositoryImpl()  // 추가
) {
    self.repository = repository
    self.userRepository = userRepository
}

// 메서드 추가
func loadMyUserId() async {
    do {
        let dto = try await userRepository.getMyProfile()
        myUserId = dto.id
    } catch {
        // QR 표시 불가 — 조용히 실패 (검색 기능에 영향 없음)
    }
}
```

`.onAppear`에서 `viewModel.loadMyUserId()` 호출 추가.

#### 4-D: Info.plist 카메라 권한 추가

**`ios/Odiya/Resources/Info.plist`** 수정:
```xml
<!-- </dict> 직전에 추가 -->
<key>NSCameraUsageDescription</key>
<string>QR 코드를 스캔하여 친구를 추가하기 위해 카메라 접근이 필요합니다.</string>
```

---

## 기술 결정

| 결정 | 이유 |
|------|------|
| Presigned URL 방식 이미지 업로드 | 서버가 이미지 바이너리를 중계하지 않아 서버 부하 최소화. 클라이언트 → S3 직접 업로드. |
| S3 모듈을 `modules/storage`로 분리 | 기존 모듈 구조(`modules/jpa`, `modules/redis`, `modules/kafka`)와 일관. 다른 앱(batch, streamer)에서도 재사용 가능. |
| Controller에서 직접 StorageService 호출 (Facade 미사용) | Presigned URL 발급은 단일 서비스 호출이므로 Facade 레이어 불필요. 기존 패턴에서도 단순 조회는 Facade 없이 처리하는 경우 있음. |
| ImageUploadService를 actor로 구현 | APIClient 패턴(actor, static shared)과 일관. 동시성 안전. |
| QR 데이터 형식: `odiya://friend?userId={id}` | 앱 내부 전용 스킴. URL 기반이므로 향후 딥링크 확장 용이. |
| QR userId를 GET /api/v1/users/me로 조회 | PRD 결정사항 준수. 별도 API 불필요. |
| PhotosPicker (iOS 16+) 사용 | SwiftUI 네이티브 이미지 선택 API. 프로젝트 최소 타겟 iOS 17이므로 문제 없음. |
| QRScannerViewModel에서 직접 FriendRepository 호출 | 기존 AddFriendViewModel에서 `repository.sendFriendRequest(targetUserId:)`를 직접 호출하는 패턴과 동일. |
| ProfileView에서 SettingsView 패턴으로 onLogout 주입 | 기존 코드베이스에서 검증된 패턴. EnvironmentObject + .onAppear 콜백 주입 방식. |

---

## 준수 규격

외부 규격 참조 파일 6개를 검토한 결과, 이번 설계에 직접 관련된 규격은 없다:
- FCM HTTP v1, 카카오 로컬/로그인/모빌리티, 카카오맵 SDK, ODsay 대중교통 -- 모두 프로필 이미지 업로드, QR 코드, 회원 탈퇴 화면 전환과 무관.

---

## 확인이 필요한 사항

1. **S3 버킷 설정**: 프로필 이미지용 S3 버킷이 이미 생성되어 있는지, public read 정책 또는 CloudFront 배포가 구성되어 있는지 확인 필요. `imageUrl`이 직접 S3 URL인지 CloudFront URL인지에 따라 StorageService의 publicUrl 생성 로직이 달라진다.
2. **AWS 크레덴셜 관리**: 배포 환경(EC2 IAM Role, ECS Task Role 등)에서 환경변수 기반 크레덴셜을 사용할 수 있는지, 또는 properties 파일로 주입해야 하는지 확인 필요.
3. **이미지 리사이징**: 클라이언트에서 업로드 전 이미지를 리사이즈할지, 원본 그대로 업로드할지 정책 결정 필요. 대용량 이미지(10MB+)에 대한 제한을 설정하는 것을 권장.

---

## 탐색 추가 항목

| 파일 | 발견 내용 |
|------|-----------|
| `ios/Odiya/UI/Navigation/ProfileMenuView.swift` | ProfileView()를 NavigationLink로 push하는 진입점. onLogout을 주입하지 않고 있으나, EnvironmentObject 패턴을 쓰면 여기를 수정할 필요 없음. |
| `ios/Odiya/UI/Navigation/MainTabView.swift` | `.sheet(isPresented: $showProfile)` 내에서 `ProfileMenuView(onLogout: { pendingLogout = true })`를 생성. sheet dismiss 후 `router.navigateToLogin()` 호출하는 패턴. ProfileView에서는 RootRouter를 직접 호출하므로 이 pendingLogout 메커니즘을 우회함 -- 이는 의도적이며 정상 동작한다 (sheet 자체가 dismiss 되기 전에 RootRouter가 `.login`으로 전환되면 sheet도 함께 사라짐). |
| `ios/Odiya/UI/Components/ProfileImageView.swift` | 공용 프로필 이미지 컴포넌트. ProfileView의 프로필 영역에서도 이를 활용할 수 있으나, 현재 ProfileView는 직접 AsyncImage를 사용 중. 리팩터링 기회이나 이번 스코프 외. |
| `ios/Odiya/Core/Network/APIClient.swift` | actor 기반 네트워크 클라이언트. `request<T: Decodable>`, `requestVoid` 두 가지 메서드만 제공. S3 PUT 업로드는 APIClient를 사용하지 않고 별도 ImageUploadService에서 직접 URLSession 호출 (S3 응답이 앱의 ApiResponse 포맷이 아니므로). |
| `backend/apps/odiya-api/build.gradle.kts` | 현재 AWS SDK 의존성 없음. storage 모듈 추가 시 이 파일에 모듈 의존성만 추가하면 됨. |
