import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ZStack {
            // Фоновый слой с темно-серым цветом, расширяющийся во все safe areas
            Color(red: 47/255.0, green: 48/255.0, blue: 51/255.0)
                .ignoresSafeArea(.all)

            // Compose контент поверх фона
            ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
        }
    }
}



