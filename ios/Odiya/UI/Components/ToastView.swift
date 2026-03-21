import SwiftUI

struct ToastView: View {

    let message: String
    let type: ToastType

    enum ToastType {
        case success
        case error
        case info

        var backgroundColor: Color {
            switch self {
            case .success: return OdiyaColors.success
            case .error:   return OdiyaColors.danger
            case .info:    return OdiyaColors.primary
            }
        }

        var iconName: String {
            switch self {
            case .success: return "checkmark.circle.fill"
            case .error:   return "exclamationmark.circle.fill"
            case .info:    return "info.circle.fill"
            }
        }
    }

    var body: some View {
        HStack(spacing: 10) {
            Image(systemName: type.iconName)
            Text(message)
                .font(.subheadline)
                .fontWeight(.medium)
        }
        .foregroundStyle(.white)
        .padding(.horizontal, 20)
        .padding(.vertical, 14)
        .background(type.backgroundColor)
        .clipShape(Capsule())
        .shadow(color: .black.opacity(0.15), radius: 8, y: 4)
    }
}

// MARK: - Toast Modifier

struct ToastModifier: ViewModifier {

    @Binding var isPresented: Bool
    let message: String
    let type: ToastView.ToastType

    func body(content: Content) -> some View {
        content.overlay(alignment: .top) {
            if isPresented {
                ToastView(message: message, type: type)
                    .padding(.top, 8)
                    .transition(.move(edge: .top).combined(with: .opacity))
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 2) {
                            withAnimation { isPresented = false }
                        }
                    }
                    .zIndex(100)
            }
        }
        .animation(.spring(response: 0.4), value: isPresented)
    }
}

extension View {
    func toast(isPresented: Binding<Bool>, message: String, type: ToastView.ToastType = .success) -> some View {
        modifier(ToastModifier(isPresented: isPresented, message: message, type: type))
    }
}

#Preview {
    VStack(spacing: 16) {
        ToastView(message: "약속이 생성되었습니다!", type: .success)
        ToastView(message: "오류가 발생했습니다", type: .error)
        ToastView(message: "출발 시간이 변경되었습니다", type: .info)
    }
}
