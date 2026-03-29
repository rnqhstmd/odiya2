import SwiftUI

struct MyAppointmentsView: View {

    @StateObject private var viewModel = MyAppointmentsViewModel()
    @State private var selectedSegment: Int = 0
    @State private var showCreateAppointment = false
    @Environment(\.showNotifications) private var showNotifications
    @Environment(\.showProfile) private var showProfile

    // MARK: - Body

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                segmentControl
                    .padding(.horizontal, 20)
                    .padding(.vertical, 12)

                Divider()

                appointmentList
            }
            .navigationTitle("내 약속")
            .navigationBarTitleDisplayMode(.large)
            .background(Color(.systemGroupedBackground))
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    HStack(spacing: 4) {
                        Button { showCreateAppointment = true } label: {
                            Image(systemName: "plus")
                        }
                        Button { showNotifications.wrappedValue = true } label: {
                            Image(systemName: "bell")
                        }
                        Button { showProfile.wrappedValue = true } label: {
                            Image(systemName: "person.circle")
                        }
                    }
                    .tint(OdiyaColors.primary)
                }
            }
            .sheet(isPresented: $showCreateAppointment, onDismiss: {
                Task { await viewModel.loadAppointments() }
            }) {
                CreateAppointmentView()
            }
            .onAppear {
                Task { await viewModel.loadAppointments() }
            }
            .refreshable {
                await viewModel.loadAppointments()
            }
            .overlay {
                if viewModel.isLoading && viewModel.upcomingAppointments.isEmpty && viewModel.pastAppointments.isEmpty {
                    ProgressView()
                }
            }
        }
    }

    // MARK: - Segment Control

    private var segmentControl: some View {
        Picker("약속 목록", selection: $selectedSegment) {
            Text("다가오는 약속").tag(0)
            Text("지난 약속").tag(1)
        }
        .pickerStyle(.segmented)
    }

    // MARK: - List

    @ViewBuilder
    private var appointmentList: some View {
        if selectedSegment == 0 {
            upcomingList
        } else {
            pastList
        }
    }

    private var upcomingList: some View {
        Group {
            if viewModel.upcomingAppointments.isEmpty {
                emptyUpcoming
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(Array(viewModel.upcomingAppointments.enumerated()), id: \.element.id) { index, appointment in
                            NavigationLink {
                                AppointmentDetailView(appointment: appointment)
                                    .navigationTitle("약속 상세")
                            } label: {
                                AppointmentCardView(
                                    appointment: appointment,
                                    isImminent: index == 0 && appointment.isImminent
                                )
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
                }
            }
        }
    }

    private var pastList: some View {
        Group {
            if viewModel.pastAppointments.isEmpty {
                emptyPast
            } else {
                ScrollView {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.pastAppointments) { appointment in
                            NavigationLink {
                                AppointmentDetailView(appointment: appointment)
                                    .navigationTitle("약속 상세")
                            } label: {
                                AppointmentCardView(
                                    appointment: appointment,
                                    isImminent: false
                                )
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 16)
                }
            }
        }
    }

    // MARK: - Empty States

    private var emptyUpcoming: some View {
        Spacer()
            .frame(maxHeight: .infinity)
            .overlay(
                EmptyStateView(
                    iconName: "calendar.badge.plus",
                    title: "다가오는 약속이 없어요",
                    description: "새로운 약속을 만들어보세요",
                    actionTitle: "약속 만들기",
                    action: {
                        showCreateAppointment = true
                    }
                )
            )
    }

    private var emptyPast: some View {
        Spacer()
            .frame(maxHeight: .infinity)
            .overlay(
                EmptyStateView(
                    iconName: "clock.arrow.circlepath",
                    title: "지난 약속이 없어요",
                    description: "약속을 잡고 추억을 만들어보세요"
                )
            )
    }
}

#Preview {
    MyAppointmentsView()
}
