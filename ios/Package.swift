// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "Odiya",
    platforms: [
        .iOS(.v16)
    ],
    dependencies: [
        .package(url: "https://github.com/kakao/kakao-ios-sdk.git", from: "2.22.0")
    ],
    targets: [
        .executableTarget(
            name: "Odiya",
            dependencies: [
                .product(name: "KakaoSDK", package: "kakao-ios-sdk")
            ],
            path: "Odiya"
        ),
        .testTarget(
            name: "OdiyaTests",
            dependencies: ["Odiya"],
            path: "OdiyaTests"
        )
    ]
)
