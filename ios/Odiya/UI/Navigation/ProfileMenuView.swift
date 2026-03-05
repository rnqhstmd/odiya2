import SwiftUI

struct ProfileMenuView: View {

    @Environment(\.dismiss) private var dismiss
    @State private var showLogoutAlert = false

    private let user = MockData.currentUser

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 프로필 영역
                profileSection

                Divider()

                // 설정
                settingsSection

                Divider()

                // 로그아웃
                logoutSection

                Spacer()
            }
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("닫기") {
                        dismiss()
                    }
                    .tint(OdiyaColors.primary)
                }
            }
            .alert("로그아웃", isPresented: $showLogoutAlert) {
                Button("취소", role: .cancel) {}
                Button("로그아웃", role: .destructive) {
                    // TODO: 로그아웃 처리
                    dismiss()
                }
            } message: {
                Text("정말 로그아웃하시겠어요?")
            }
        }
    }

    // MARK: - Profile Section

    private var profileSection: some View {
        NavigationLink {
            ProfileView()
        } label: {
            HStack(spacing: 16) {
                ProfileImageView(imageUrl: user.profileImageUrl, size: 56)

                VStack(alignment: .leading, spacing: 4) {
                    Text(user.nickname)
                        .font(.headline)
                        .foregroundColor(.primary)
                    Text("내 프로필")
                        .font(.subheadline)
                        .foregroundColor(OdiyaColors.primary)
                }

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 20)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Settings Section

    private var settingsSection: some View {
        NavigationLink {
            SettingsView()
        } label: {
            HStack(spacing: 16) {
                Image(systemName: "gearshape")
                    .font(.system(size: 20))
                    .foregroundColor(OdiyaColors.primary)
                    .frame(width: 28)

                Text("설정")
                    .font(.body)
                    .foregroundColor(.primary)

                Spacer()

                Image(systemName: "chevron.right")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.secondary)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
        }
        .buttonStyle(.plain)
    }

    // MARK: - Logout Section

    private var logoutSection: some View {
        Button {
            showLogoutAlert = true
        } label: {
            HStack(spacing: 16) {
                Image(systemName: "rectangle.portrait.and.arrow.right")
                    .font(.system(size: 20))
                    .foregroundColor(OdiyaColors.danger)
                    .frame(width: 28)

                Text("로그아웃")
                    .font(.body)
                    .foregroundColor(OdiyaColors.danger)

                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 18)
        }
    }
}

#Preview {
    ProfileMenuView()
}
