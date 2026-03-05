import XCTest

final class LoginFlowUITests: XCTestCase {

    private var app: XCUIApplication!

    override func setUp() {
        super.setUp()
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDown() {
        app = nil
        super.tearDown()
    }

    func test_로그인_화면_표시() {
        // 카카오 로그인 버튼이 표시되는지 확인
        let kakaoButton = app.buttons["카카오로 시작하기"]
        XCTAssertTrue(kakaoButton.waitForExistence(timeout: 5))
    }

    func test_앱_로고_표시() {
        // 앱 타이틀이 표시되는지 확인
        let title = app.staticTexts["오디야"]
        XCTAssertTrue(title.waitForExistence(timeout: 5))
    }
}
