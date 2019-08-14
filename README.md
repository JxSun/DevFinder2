# DevFinder2

## Architecture

This app adopts the Model-View-Intent (MVI) pattern which enforces the app to be reactive and has only one unidirectional logic processing flow, so that we have a clear and centralized state management.

To follow the idea, the app is conceptually divided into 3 layers:  
- UI layer:  
This is where the Activities, Fragments and other UI widgets stay. All `UiEvent`s will be delivered to the domain layer by calling the ViewModel's `fireEvent()`, and the UI can also observe the resulted `UiState`s to render UI accordingly.
- Domain layer:  
This layer includes the business logic components like ViewModels and ActionProcessors. The input `UiEvent`s will be converted to the logical `Action`s first and then be sent to the ActionProcessors, and the processed `Result`s will be finally converted to `UiState`s for UI layer to render.
- Data layer:  
The data accessing related components like Repositories, DataSources are in this layer. The Repositories are the interfaces to cooperate with the domain layer.
