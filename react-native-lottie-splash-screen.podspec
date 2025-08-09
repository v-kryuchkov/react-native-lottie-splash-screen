require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-lottie-splash-screen"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.author       = 'taehyun'
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.source       = { :git => "https://github.com/attarchi/react-native-lottie-splash-screen", :tag => "v#{s.version}" }
  
  # Platform and deployment targets
  s.ios.deployment_target = '13.0'
  s.swift_version = '5.7'
  
  # Source files - include both Swift and Objective-C files
  s.source_files = "ios/*.{h,m,swift}"
  
  # Module configuration to make Swift accessible
  s.pod_target_xcconfig = {
    'SWIFT_VERSION' => '5.7',
    'CLANG_ENABLE_MODULES' => 'YES',
    'DEFINES_MODULE' => 'YES'
  }
  
  # Dependencies
  s.dependency "React"
  s.dependency "lottie-ios"
  
  # Module configuration - match the Swift class name
  s.module_name = 'SplashScreen'
  
  # Framework dependencies
  s.ios.frameworks = ['UIKit', 'CoreGraphics']
  
  # Ensure the module is properly exposed
  s.xcconfig = {
    'SWIFT_INCLUDE_PATHS' => '$(PODS_ROOT)/react-native-lottie-splash-screen/ios'
  }
end
