#!/bin/bash

# =============================================================================
# Build APK and Create GitHub Release Script
# =============================================================================

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$SCRIPT_DIR"

# APK output path
APK_OUTPUT_DIR="$PROJECT_DIR/app/build/outputs/apk/release"
APK_NAME="app-release.apk"

# =============================================================================
# Helper Functions
# =============================================================================

print_header() {
    echo -e "\n${CYAN}═══════════════════════════════════════════════════════════════${NC}" >&2
    echo -e "${CYAN}  $1${NC}" >&2
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════${NC}\n" >&2
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}" >&2
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}" >&2
}

print_error() {
    echo -e "${RED}✗ $1${NC}" >&2
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}" >&2
}

# =============================================================================
# Check Prerequisites
# =============================================================================

check_prerequisites() {
    print_header "Checking Prerequisites"
    
    # Check if gh CLI is installed
    if ! command -v gh &> /dev/null; then
        print_error "GitHub CLI (gh) is not installed."
        print_info "Please install it from: https://cli.github.com/"
        print_info "Or run: sudo apt install gh (on Ubuntu/Debian)"
        print_info "Or: brew install gh (on macOS)"
        exit 1
    fi
    print_success "GitHub CLI (gh) is installed"
    
    # Check if user is authenticated with gh
    if ! gh auth status &> /dev/null; then
        print_error "You are not authenticated with GitHub CLI"
        print_info "Please run: gh auth login"
        exit 1
    fi
    print_success "GitHub CLI is authenticated"
    
    # Check if we're in a git repository
    if ! git rev-parse --git-dir > /dev/null 2>&1; then
        print_error "Not a git repository"
        exit 1
    fi
    print_success "Git repository detected"
    
    # Check if gradlew exists and is executable
    if [ ! -f "$PROJECT_DIR/gradlew" ]; then
        print_error "gradlew not found in $PROJECT_DIR"
        exit 1
    fi
    
    if [ ! -x "$PROJECT_DIR/gradlew" ]; then
        print_info "Making gradlew executable..."
        chmod +x "$PROJECT_DIR/gradlew"
    fi
    print_success "Gradle wrapper is ready"
    
    # Setup Java - prefer SDKMAN Java 21 if available
    setup_java
    
    # Ensure debug keystore exists
    ensure_keystore
}

# =============================================================================
# Setup Java - Use SDKMAN Java 21 or warn about version
# =============================================================================

setup_java() {
    print_info "Checking Java version..."
    
    local java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | head -1)
    print_info "Current Java version: $java_version"
    
    # Check if SDKMAN is available with Java 21
    if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        source "$HOME/.sdkman/bin/sdkman-init.sh" 2>/dev/null
        if sdk list java 2>/dev/null | grep -q "21.*installed"; then
            print_info "Switching to Java 21 via SDKMAN..."
            sdk use java 21.0.7-tem 2>/dev/null || sdk use java 21.0.6-tem 2>/dev/null || sdk use java 21-tem 2>/dev/null
            java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | head -1)
            print_success "Now using Java $java_version"
        else
            print_warning "SDKMAN Java 21 not found. Current Java: $java_version"
            print_info "For best results, install Java 21: sdk install java 21.0.7-tem"
        fi
    else
        # Check if system Java is compatible (Java 17, 21)
        if [[ "$java_version" =~ ^26\. ]]; then
            print_warning "Java 26 detected. Gradle may have compatibility issues."
            print_info "Consider installing Java 21 using SDKMAN:"
            print_info "  curl -s \"https://get.sdkman.io\" | bash"
            print_info "  source \"$HOME/.sdkman/bin/sdkman-init.sh\""
            print_info "  sdk install java 21.0.7-tem"
            echo ""
            print_info "Attempting build with Java 26 anyway..."
        elif [[ "$java_version" =~ ^17\..* ]] || [[ "$java_version" =~ ^21\..* ]]; then
            print_success "Java $java_version is compatible"
        else
            print_warning "Java $java_version - compatibility not guaranteed"
        fi
    fi
}

# =============================================================================
# Ensure Debug Keystore Exists
# =============================================================================

ensure_keystore() {
    local keystore_dir="$HOME/.android"
    local keystore_file="$keystore_dir/debug.keystore"
    
    if [ ! -f "$keystore_file" ]; then
        print_info "Creating debug keystore..."
        mkdir -p "$keystore_dir"
        keytool -genkey -v \
            -keystore "$keystore_file" \
            -storepass android \
            -alias androiddebugkey \
            -keypass android \
            -keyalg RSA \
            -keysize 2048 \
            -validity 10000 \
            -dname "CN=Android Debug,O=Android,C=US" 2>&1 | tail -3
        print_success "Debug keystore created at $keystore_file"
    else
        print_success "Debug keystore exists"
    fi
}

# =============================================================================
# Get Current Version from build.gradle
# =============================================================================

get_current_version() {
    local version_name=$(grep -oP 'versionName "\K[^"]+' "$PROJECT_DIR/app/build.gradle" || echo "unknown")
    local version_code=$(grep -oP 'versionCode \K[0-9]+' "$PROJECT_DIR/app/build.gradle" || echo "0")
    echo "$version_name (code: $version_code)"
}

# =============================================================================
# Get Latest GitHub Release
# =============================================================================

get_latest_release() {
    print_header "Checking Latest GitHub Release"
    
    # Get the repository from git remote
    local repo_url=$(git remote get-url origin 2>/dev/null || echo "")
    local repo=""
    
    if [[ "$repo_url" =~ github.com[:/]([^/]+/[^/\.]+)(\.git)?$ ]]; then
        repo="${BASH_REMATCH[1]}"
    fi
    
    if [ -z "$repo" ]; then
        print_error "Could not determine GitHub repository from remote URL"
        print_info "Remote URL: $repo_url"
        exit 1
    fi
    
    print_info "Repository: $repo"
    
    # Get the latest release using gh CLI
    local latest_release=$(gh release list --repo "$repo" --limit 1 2>/dev/null | awk '{print $1}' || echo "")
    
    if [ -z "$latest_release" ]; then
        print_warning "No existing releases found in this repository"
        latest_release="none"
    else
        print_info "Latest release: $latest_release"
    fi
    
    # Get current app version from build.gradle
    local current_version=$(get_current_version)
    print_info "Current app version in build.gradle: $current_version"
    
    echo "$latest_release"
}

# =============================================================================
# Prompt for New Release Version
# =============================================================================

prompt_new_version() {
    local latest_release=$1
    
    print_header "Enter New Release Version"
    
    echo -e "${YELLOW}Latest GitHub release: ${NC}${latest_release}" >&2
    echo "" >&2
    
    # Suggest next version based on latest
    local suggested_version=""
    if [ "$latest_release" != "none" ] && [[ "$latest_release" =~ ^v?([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
        local major="${BASH_REMATCH[1]}"
        local minor="${BASH_REMATCH[2]}"
        local patch="${BASH_REMATCH[3]}"
        local next_patch=$((patch + 1))
        suggested_version="v${major}.${minor}.${next_patch}"
    elif [ "$latest_release" != "none" ] && [[ "$latest_release" =~ ^v?([0-9]+)\.([0-9]+)$ ]]; then
        local major="${BASH_REMATCH[1]}"
        local minor="${BASH_REMATCH[2]}"
        local next_minor=$((minor + 1))
        suggested_version="v${major}.${next_minor}"
    else
        suggested_version="v1.0.0"
    fi
    
    echo -e "${CYAN}Suggested next version: ${NC}${suggested_version}" >&2
    echo "" >&2
    
    while true; do
        echo -ne "${YELLOW}Enter the new release version (e.g., v1.0.0, 1.2.3): ${NC}" >&2
        read -r new_version
        
        # Remove leading/trailing whitespace
        new_version=$(echo "$new_version" | xargs)
        
        if [ -z "$new_version" ]; then
            print_error "Version cannot be empty. Please try again."
            continue
        fi
        
        # Add 'v' prefix if not present
        if [[ ! "$new_version" =~ ^v ]]; then
            new_version="v${new_version}"
        fi
        
        # Validate version format
        if [[ ! "$new_version" =~ ^v[0-9]+(\.[0-9]+)*$ ]]; then
            print_warning "Version format looks unusual. Valid examples: v1.0.0, v2.1.3, v1.0"
            echo -ne "${YELLOW}Do you want to proceed with '$new_version'? (y/n): ${NC}" >&2
            read -r confirm
            if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
                continue
            fi
        fi
        
        # Check if release already exists
        local repo_url=$(git remote get-url origin 2>/dev/null || echo "")
        local repo=""
    if [[ "$repo_url" =~ github.com[:/]([^/]+/[^/\.]+)(\.git)?$ ]]; then
            repo="${BASH_REMATCH[1]}"
        fi
        
        if gh release view "$new_version" --repo "$repo" &> /dev/null 2>&1; then
            print_error "Release $new_version already exists!"
            print_info "Please choose a different version number."
            continue
        fi
        
        break
    done
    
    echo "" >&2
    echo -ne "${YELLOW}Enter release title (optional, press Enter to use version): ${NC}" >&2
    read -r release_title
    
    if [ -z "$release_title" ]; then
        release_title="Release $new_version"
    fi
    
    echo "" >&2
    echo -e "${CYAN}Release notes (press Enter twice to finish):${NC}" >&2
    local release_notes=""
    while IFS= read -r line; do
        [ -z "$line" ] && break
        release_notes="${release_notes}${line}\n"
    done
    
    if [ -z "$release_notes" ]; then
        release_notes="Release ${new_version}"
    fi
    
    echo "" >&2
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}" >&2
    echo -e "${GREEN}  New Release Summary:${NC}" >&2
    echo -e "${GREEN}═══════════════════════════════════════════════════════════════${NC}" >&2
    echo -e "  ${BLUE}Version:${NC} $new_version" >&2
    echo -e "  ${BLUE}Title:${NC}   $release_title" >&2
    echo -e "  ${BLUE}Notes:${NC}   $release_notes" >&2
    echo "" >&2
    
    echo -ne "${YELLOW}Proceed with creating this release? (y/n): ${NC}" >&2
    read -r confirm
    
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        print_info "Release cancelled by user"
        exit 0
    fi
    
    echo "$new_version|$release_title|$release_notes"
}

# =============================================================================
# Build APK
# =============================================================================

build_apk() {
    print_header "Building APK"
    
    cd "$PROJECT_DIR"
    
    # Source SDKMAN if available to ensure correct Java version
    if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
        source "$HOME/.sdkman/bin/sdkman-init.sh" 2>/dev/null
        sdk use java 21.0.7-tem 2>/dev/null || sdk use java 21.0.6-tem 2>/dev/null || sdk use java 21-tem 2>/dev/null || true
    fi
    
    # Clean first
    print_info "Cleaning previous builds..."
    ./gradlew clean --quiet 2>/dev/null || true
    
    # Build release APK
    print_info "Building release APK... (this may take a few minutes)"
    ./gradlew assembleRelease
    
    if [ ! -f "$APK_OUTPUT_DIR/$APK_NAME" ]; then
        # Try to find the APK with a different name pattern
        local found_apk=$(find "$APK_OUTPUT_DIR" -name "*.apk" -type f 2>/dev/null | head -n 1)
        if [ -n "$found_apk" ]; then
            APK_NAME=$(basename "$found_apk")
            print_info "Found APK: $APK_NAME"
        else
            print_error "APK build failed or APK not found in $APK_OUTPUT_DIR"
            print_info "Expected: $APK_OUTPUT_DIR/$APK_NAME"
            exit 1
        fi
    fi
    
    print_success "APK built successfully!"
    print_info "Location: $APK_OUTPUT_DIR/$APK_NAME"
    
    # Show APK size
    local apk_size=$(du -h "$APK_OUTPUT_DIR/$APK_NAME" | cut -f1)
    print_info "Size: $apk_size"
}

# =============================================================================
# Create GitHub Release and Upload APK
# =============================================================================

create_release() {
    local version="$1"
    local title="$2"
    local notes="$3"
    
    print_header "Creating GitHub Release"
    
    local repo_url=$(git remote get-url origin 2>/dev/null || echo "")
    local repo=""
    if [[ "$repo_url" =~ github.com[:/]([^/]+/[^/\.]+)(\.git)?$ ]]; then
        repo="${BASH_REMATCH[1]}"
    fi
    
    if [ -z "$repo" ]; then
        print_error "Could not determine GitHub repository"
        exit 1
    fi
    
    print_info "Creating release $version for $repo..."
    print_info "Title: $title"
    
    # Create release with notes
    # Note: We use -F - to read notes from stdin
    echo -e "$notes" | gh release create "$version" \
        --repo "$repo" \
        --title "$title" \
        --notes-file - \
        --draft=false \
        --prerelease=false
    
    print_success "Release created successfully!"
    
    # Upload APK to release
    print_info "Uploading APK to release..."
    
    gh release upload "$version" \
        --repo "$repo" \
        "$APK_OUTPUT_DIR/$APK_NAME"
    
    print_success "APK uploaded successfully!"
    
    # Show release URL
    local release_url="https://github.com/$repo/releases/tag/$version"
    echo "" >&2
    print_success "Release published!"
    echo -e "${CYAN}Release URL: ${NC}$release_url" >&2
    echo "" >&2
    
    # Optionally open in browser
    echo -ne "${YELLOW}Open release in browser? (y/n): ${NC}" >&2
    read -r open_browser
    
    if [[ "$open_browser" =~ ^[Yy]$ ]]; then
        if command -v xdg-open &> /dev/null; then
            xdg-open "$release_url"
        elif command -v open &> /dev/null; then
            open "$release_url"
        else
            print_warning "Could not open browser automatically"
            print_info "Please visit: $release_url"
        fi
    fi
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    echo -e "${CYAN}" >&2
    echo "╔═══════════════════════════════════════════════════════════════╗" >&2
    echo "║           🚀 APK Builder & GitHub Release Creator             ║" >&2
    echo "╚═══════════════════════════════════════════════════════════════╝" >&2
    echo -e "${NC}" >&2
    
    # Check prerequisites
    check_prerequisites
    
    # Get latest release
    latest_release=$(get_latest_release)
    
    # Prompt for new version
    IFS='|' read -r new_version release_title release_notes <<< "$(prompt_new_version "$latest_release")"
    
    # Build APK
    build_apk
    
    # Create release and upload APK
    create_release "$new_version" "$release_title" "$release_notes"
    
    print_header "All Done! 🎉"
    echo -e "${GREEN}Your APK has been built and released!${NC}" >&2
    echo "" >&2
}

# Run main function
main "$@"
