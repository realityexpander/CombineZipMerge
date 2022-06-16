package com.example.combinezipmerge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel: ViewModel() {

    // flows are allowed in the viewModel.
    // ViewModel cannot use Compose State.
    // We use collectAsState to collect the flow as state for the Compose UI.

    ///// for .combine example /////
    private val isAuthenticated = MutableStateFlow<Boolean>(false)

    private val user = MutableStateFlow<User?>(null) // from one api
    private val posts = MutableStateFlow(emptyList<Post>()) // from another api

    private val _profileState = MutableStateFlow<ProfileState?>(null)
    val profileState = _profileState.asStateFlow()



    //// for .zip & .merge example ////
    private val flow1 = (1..10).asFlow().onEach {  delay(1000L) }
    private val flow2 = (10..20).asFlow().onEach {  delay(300L) }
    var numberString by mutableStateOf("")
        private set


    init {


        ///// for .combine example ////

        // Collects any updates to user or posts into a single state using the latest data.
        // Does not wait for the other flow to emit a value.
        user.combine(posts) { user, posts ->
            _profileState.value = profileState.value?.copy(
                profilePicUrl = user?.profilePicUrl,
                username = user?.username,
                description = user?.description,
                posts = posts
            )
        }.launchIn(viewModelScope)

//        // Above is the same as: (but above saves one level of indentation)
//        viewModelScope.launch {
//            user.combine(posts) { user, posts ->
//                _profileState.value = profileState.value?.copy (
//                    profilePicUrl = user?.profilePicUrl,
//                    username = user?.username,
//                    description = user?.description,
//                    posts = posts
//                )
//            }.collect()
//        }

        // To combine 3 flows, chain them together with multiple combines
        isAuthenticated.combine(user) { isAuthenticated, user ->
            if (isAuthenticated) {
                user
            } else {
                null
            }
        // using onEach displays the type passed from previous flow in IDE
        }.onEach {
        }.combine(posts) { user, posts ->

            user?.let {
                _profileState.value = profileState.value?.copy(
                    username = user.username,
                    profilePicUrl = user.profilePicUrl,
                    description = user.description,
                    posts = posts
                )
            }
        }.launchIn(viewModelScope)



        ///// for .zip example //////

        // Collects exact Pairs of the flow of numbers.
        // Zip waits for both flows to emit a value before combining them.
        flow1.zip(flow2) { a, b ->
            numberString += "($a $b)\n"
        }.launchIn(viewModelScope)



        ///// for .merge example //////

        // Merge collects every values from both flows as they are emitted.
        // Merge does not wait for pairs.
        merge(flow1, flow2).onEach {
            numberString += "merge:$it\n"
        }.launchIn(viewModelScope)


    }
}