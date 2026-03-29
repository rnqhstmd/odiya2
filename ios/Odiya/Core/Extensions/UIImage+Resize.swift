import UIKit

extension UIImage {

    /// 이미지를 지정된 크기로 aspectFill 방식 리사이즈
    func resizedToFill(size: CGSize) -> UIImage {
        let aspectWidth = size.width / self.size.width
        let aspectHeight = size.height / self.size.height
        let scale = max(aspectWidth, aspectHeight)

        let scaledSize = CGSize(
            width: self.size.width * scale,
            height: self.size.height * scale
        )

        let origin = CGPoint(
            x: (size.width - scaledSize.width) / 2,
            y: (size.height - scaledSize.height) / 2
        )

        let renderer = UIGraphicsImageRenderer(size: size)
        return renderer.image { _ in
            self.draw(in: CGRect(origin: origin, size: scaledSize))
        }
    }
}
