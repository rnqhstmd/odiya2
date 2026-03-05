import SwiftUI

struct TagManagementView: View {

    @ObservedObject var viewModel: SettingsViewModel
    @State private var showAddSheet = false
    @State private var editingTag: Tag? = nil
    @State private var tagToDelete: Tag? = nil
    @State private var showDeleteAlert = false

    var body: some View {
        List {
            ForEach(viewModel.tags) { tag in
                Button {
                    editingTag = tag
                } label: {
                    HStack(spacing: 12) {
                        TagDotView(color: tag.color, size: 16)
                        Text(tag.name)
                            .foregroundStyle(.primary)
                        Spacer()
                        if tag.isDefault {
                            Text("기본")
                                .font(.caption2)
                                .foregroundStyle(.secondary)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 3)
                                .background(Color(.systemGray5))
                                .clipShape(Capsule())
                        }
                    }
                    .padding(.vertical, 2)
                }
                .buttonStyle(.plain)
                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                    if !tag.isDefault {
                        Button(role: .destructive) {
                            tagToDelete = tag
                            showDeleteAlert = true
                        } label: {
                            Label("삭제", systemImage: "trash")
                        }
                    }
                }
            }
        }
        .navigationTitle("태그 관리")
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
            TagEditSheet(existingTag: nil) { newTag in
                viewModel.addTag(newTag)
            }
        }
        .sheet(item: $editingTag) { tag in
            TagEditSheet(existingTag: tag) { updatedTag in
                viewModel.updateTag(updatedTag)
            }
        }
        .alert("태그 삭제", isPresented: $showDeleteAlert) {
            Button("삭제", role: .destructive) {
                if let tag = tagToDelete {
                    viewModel.deleteTag(tag)
                }
                tagToDelete = nil
            }
            Button("취소", role: .cancel) {
                tagToDelete = nil
            }
        } message: {
            Text("이 태그를 삭제하시겠어요?")
        }
    }
}

#Preview {
    NavigationStack {
        TagManagementView(viewModel: SettingsViewModel())
    }
}
