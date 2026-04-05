.PHONY: all build debug release clean install test db

JAVA_HOME := /Applications/Android\ Studio.app/Contents/jbr/Contents/Home
export JAVA_HOME

all: debug

build:
	./gradlew assemble

debug: db
	./gradlew assembleDebug

release: db
	./gradlew assembleRelease

db:
	python3 scripts/convert_to_sqlite.py

clean:
	./gradlew clean

install:
	adb install -r app/build/outputs/apk/debug/app-debug.apk

test:
	./gradlew test

lint:
	./gradlew lint

help:
	@echo "Available targets:"
	@echo "  all      - Build debug APK (default)"
	@echo "  build    - Build both debug and release APKs"
	@echo "  debug    - Build debug APK (auto-updates words.db)"
	@echo "  release  - Build release APK (auto-updates words.db)"
	@echo "  db       - Update words.db from JSON"
	@echo "  clean    - Clean build outputs"
	@echo "  install  - Install debug APK to connected device"
	@echo "  test     - Run unit tests"
	@echo "  lint     - Run lint analysis"
	@echo ""
	@echo "Environment variables:"
	@echo "  JAVA_HOME - Path to JDK (default: Android Studio's embedded JDK)"
