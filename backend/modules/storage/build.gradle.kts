plugins {
    `java-library`
}

dependencies {
    api(platform("software.amazon.awssdk:bom:2.31.9"))
    api("software.amazon.awssdk:s3")
}
