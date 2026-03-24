import SwiftUI

/// 카카오맵 인증 실패 또는 좌표 없음 시 표시하는 fallback 뷰
struct MapFallbackView: View {

    let placeName: String?
    let height: CGFloat

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 14)
                .fill(OdiyaColors.odiya100)
                .frame(height: height)

            VStack(spacing: 8) {
                Image(systemName: "map.fill")
                    .font(.system(size: 32))
                    .foregroundStyle(OdiyaColors.odiya300)

                if let name = placeName {
                    Text(name)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(OdiyaColors.odiya900)
                }

                Text("지도를 불러올 수 없습니다")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }
}

#Preview {
    MapFallbackView(placeName: "강남역 2번 출구", height: 220)
        .padding()
}
