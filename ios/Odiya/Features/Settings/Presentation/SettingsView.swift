import SwiftUI

struct SettingsView: View {

    @StateObject private var viewModel = SettingsViewModel()
    @EnvironmentObject private var router: RootRouter

    var body: some View {
        List {
            // MARK: - 출발지 관리
            Section("출발지") {
                NavigationLink(destination: DeparturePlaceManagementView(viewModel: viewModel)) {
                    VStack(alignment: .leading, spacing: 4) {
                        Label("출발지 관리", systemImage: "mappin.and.ellipse")
                        Text("자주 가는 출발지를 관리하세요")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            // MARK: - 태그 관리
            Section("태그") {
                NavigationLink(destination: TagManagementView(viewModel: viewModel)) {
                    VStack(alignment: .leading, spacing: 4) {
                        Label("태그 관리", systemImage: "tag.fill")
                        Text("모임을 구분할 태그를 관리하세요")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
            }

            // MARK: - 계정
            Section("계정") {
                Button {
                    viewModel.showLogoutAlert = true
                } label: {
                    Text("로그아웃")
                        .foregroundStyle(.primary)
                }

                Button(role: .destructive) {
                    viewModel.showDeleteAlert = true
                } label: {
                    Text("회원 탈퇴")
                }
            }

            // MARK: - 앱 버전
            Section {
                HStack {
                    Spacer()
                    Text("오디야 v\(Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")")
                        .font(.caption)
                        .foregroundStyle(.tertiary)
                    Spacer()
                }
                .listRowBackground(Color.clear)
            }
        }
        .navigationTitle("설정")
        .onAppear {
            viewModel.onLogout = { router.navigateToLogin() }
        }
        .task {
            await viewModel.loadData()
        }
        .alert("로그아웃", isPresented: $viewModel.showLogoutAlert) {
            Button("로그아웃", role: .destructive) { viewModel.logout() }
            Button("취소", role: .cancel) {}
        } message: {
            Text("정말 로그아웃하시겠어요?")
        }
        .alert("회원 탈퇴", isPresented: $viewModel.showDeleteAlert) {
            Button("탈퇴하기", role: .destructive) { viewModel.deleteAccount() }
            Button("취소", role: .cancel) {}
        } message: {
            Text("탈퇴하면 모든 데이터가 삭제됩니다. 정말 탈퇴하시겠어요?")
        }
    }
}

#Preview {
    NavigationStack {
        SettingsView()
    }
}
