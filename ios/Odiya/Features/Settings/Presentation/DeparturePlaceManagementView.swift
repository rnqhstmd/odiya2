import SwiftUI

struct DeparturePlaceManagementView: View {

    @ObservedObject var viewModel: SettingsViewModel
    @State private var showAddSheet = false
    @State private var editingPlace: DeparturePlace? = nil
    @State private var showDeleteAlert = false
    @State private var deleteTarget: DeparturePlace? = nil

    var body: some View {
        List {
            ForEach(viewModel.departurePlaces) { place in
                Button {
                    editingPlace = place
                } label: {
                    HStack(spacing: 12) {
                        Image(systemName: place.iconName)
                            .foregroundStyle(OdiyaColors.primary)
                            .frame(width: 28)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(place.label)
                                .font(.body)
                                .foregroundStyle(.primary)
                            Text(place.address)
                                .font(.caption)
                                .foregroundStyle(.secondary)
                        }
                        Spacer()
                    }
                    .padding(.vertical, 2)
                }
                .buttonStyle(.plain)
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    Button(role: .destructive) {
                        deleteTarget = place
                        showDeleteAlert = true
                    } label: {
                        Label("삭제", systemImage: "trash")
                    }
                }
            }
        }
        .navigationTitle("출발지 관리")
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button {
                    showAddSheet = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showAddSheet) {
            DeparturePlaceEditSheet(existingPlace: nil) { newPlace in
                viewModel.addDeparturePlace(newPlace)
            }
        }
        .sheet(item: $editingPlace) { place in
            DeparturePlaceEditSheet(existingPlace: place) { updatedPlace in
                if let index = viewModel.departurePlaces.firstIndex(where: { $0.id == updatedPlace.id }) {
                    viewModel.departurePlaces[index] = updatedPlace
                }
            }
        }
        .alert("출발지 삭제", isPresented: $showDeleteAlert) {
            Button("삭제", role: .destructive) {
                if let target = deleteTarget {
                    viewModel.deleteDeparturePlace(target)
                    deleteTarget = nil
                }
            }
            Button("취소", role: .cancel) {
                deleteTarget = nil
            }
        } message: {
            if let target = deleteTarget {
                Text("\"\(target.label)\" 출발지를 삭제하시겠어요?")
            }
        }
    }
}

#Preview {
    NavigationStack {
        DeparturePlaceManagementView(viewModel: SettingsViewModel())
    }
}
