import SwiftUI

struct ProfileImageView: View {

    let imageUrl: String?
    var size: CGFloat = 40
    var nickname: String? = nil

    var body: some View {
        if let url = imageUrl, let imageURL = URL(string: url) {
            AsyncImage(url: imageURL) { image in
                image
                    .resizable()
                    .scaledToFill()
            } placeholder: {
                placeholderView
            }
            .frame(width: size, height: size)
            .clipShape(Circle())
        } else {
            placeholderView
                .frame(width: size, height: size)
        }
    }

    @ViewBuilder
    private var placeholderView: some View {
        if let nickname, let initial = nickname.first {
            ZStack {
                OdiyaColors.initialAvatarGradient
                Text(String(initial))
                    .font(.system(size: size * 0.4, weight: .bold))
                    .foregroundStyle(.white)
            }
            .frame(width: size, height: size)
            .clipShape(Circle())
        } else {
            Image(systemName: "person.circle.fill")
                .resizable()
                .foregroundStyle(OdiyaColors.odiya300)
                .frame(width: size, height: size)
        }
    }
}

/// 참여자 프로필 이미지 스택 (최대 4명 + 나머지 수)
struct ParticipantStackView: View {

    let participants: [Participant]
    var imageSize: CGFloat = 28
    var maxDisplay: Int = 4

    var body: some View {
        HStack(spacing: -8) {
            ForEach(Array(participants.prefix(maxDisplay).enumerated()), id: \.element.id) { index, participant in
                ProfileImageView(imageUrl: participant.profileImageUrl, size: imageSize, nickname: participant.nickname)
                    .overlay(
                        Circle().stroke(Color(.systemBackground), lineWidth: 2)
                    )
                    .zIndex(Double(maxDisplay - index))
            }

            if participants.count > maxDisplay {
                Text("+\(participants.count - maxDisplay)")
                    .font(.caption2)
                    .fontWeight(.semibold)
                    .foregroundStyle(.secondary)
                    .frame(width: imageSize, height: imageSize)
                    .background(Color(.systemGray5))
                    .clipShape(Circle())
                    .overlay(
                        Circle().stroke(Color(.systemBackground), lineWidth: 2)
                    )
            }
        }
    }
}

#Preview {
    VStack(spacing: 20) {
        ProfileImageView(imageUrl: nil, size: 64)
        ParticipantStackView(participants: MockData.participants)
    }
}
