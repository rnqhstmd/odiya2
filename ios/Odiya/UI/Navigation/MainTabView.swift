import SwiftUI

// MARK: - Environment Keys

private struct ShowNotificationsKey: EnvironmentKey {
    static let defaultValue: Binding<Bool> = .constant(false)
}
private struct ShowProfileKey: EnvironmentKey {
    static let defaultValue: Binding<Bool> = .constant(false)
}
extension EnvironmentValues {
    var showNotifications: Binding<Bool> {
        get { self[ShowNotificationsKey.self] }
        set { self[ShowNotificationsKey.self] = newValue }
    }
    var showProfile: Binding<Bool> {
        get { self[ShowProfileKey.self] }
        set { self[ShowProfileKey.self] = newValue }
    }
}

// MARK: - MainTabView

struct MainTabView: View {

    @State private var showNotifications = false
    @State private var showProfile = false

    var body: some View {
        TabView {
            CalendarView()
                .tabItem {
                    Label("캘린더", systemImage: "calendar")
                }

            MyAppointmentsView()
                .tabItem {
                    Label("약속", systemImage: "calendar.badge.clock")
                }

            FriendListView()
                .tabItem {
                    Label("친구", systemImage: "person.2")
                }
        }
        .tint(OdiyaColors.primary)
        .environment(\.showNotifications, $showNotifications)
        .environment(\.showProfile, $showProfile)
        .sheet(isPresented: $showNotifications) {
            NotificationListView()
        }
        .sheet(isPresented: $showProfile) {
            ProfileMenuView()
        }
    }
}

#Preview {
    MainTabView()
}
