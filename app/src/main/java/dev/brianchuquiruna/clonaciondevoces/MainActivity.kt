package dev.brianchuquiruna.clonaciondevoces

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import dev.brianchuquiruna.clonaciondevoces.databinding.ActivityMainBinding
import dev.brianchuquiruna.clonaciondevoces.learning.TopicLearnedResponse
import dev.brianchuquiruna.clonaciondevoces.util.PerfilActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import java.lang.Exception

val Context.dataStore by preferencesDataStore(name = "VOICE_ID")

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
//    private lateinit var retrofit: Retrofit
//    private lateinit var mediaPlayer:MediaPlayer
//    private lateinit var pathVoice:String
//    private var starStopVoice:Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        retrofit = GetRetrofit()
//        val createRetrofit = retrofit.create(ApiService::class.java)
//        mediaPlayer = initMediaPlayer()
//        getAllVoices(createRetrofit)
//        initListeners(createRetrofit)
    }

    fun hideKeyboard() {//activity: Activity
        val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val currentFocusView = this.currentFocus

        if (currentFocusView != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    suspend fun saveName( voiceItemResponse: VoiceItemResponse){
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(NAME)] = voiceItemResponse.name
        }
    }

    fun getName() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(NAME)].orEmpty()
    }

     suspend fun saveVoiceId( voiceItemResponse: VoiceItemResponse){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(VOICE_ID)] = voiceItemResponse.voiceId
        }
    }

     fun getVoiceId() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(VOICE_ID)].orEmpty()
    }

    suspend fun saveTeacherEmail( email: String){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(FIREBASE_EMAIL)] = email
        }
    }

    fun getTeacherEmail() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(FIREBASE_EMAIL)].orEmpty()
    }

    suspend fun savePerfilActive( perfil: PerfilActive){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(PERFIL_ACTIVE)] = perfil.name
        }
    }

    fun getPerfilActive() = dataStore.data.map { preferences ->
        PerfilActive.valueOf(preferences[stringPreferencesKey(PERFIL_ACTIVE)].orEmpty())
    }

    suspend fun saveStudentEmail( email: String){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(FIREBASE_EMAIL_STUDENT)] = email
        }
    }

    fun getStudentEmail() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(FIREBASE_EMAIL_STUDENT)].orEmpty()
    }

    suspend fun saveParentEmail( email: String){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(FIREBASE_EMAIL_PARENT)] = email
        }
    }

    fun getParentEmail() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(FIREBASE_EMAIL_PARENT)].orEmpty()
    }

    suspend fun saveSesionId( topicLearnedResponse: TopicLearnedResponse){
        dataStore.edit {preferences ->
            preferences[stringPreferencesKey(SESSION_ID)] = topicLearnedResponse.id
        }
    }

    fun getSesionId() = dataStore.data.map { preferences ->
        preferences[stringPreferencesKey(SESSION_ID)].orEmpty()
    }



//    private fun initMediaPlayer() : MediaPlayer{
//        mediaPlayer = MediaPlayer()
//        val audioAttributes = AudioAttributes.Builder()
//            .setUsage(AudioAttributes.USAGE_MEDIA) // Tipo de uso (reproducción de medios)
//            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC) // Tipo de contenido (música)
//            .build()
//        mediaPlayer.setAudioAttributes(audioAttributes)
//        return mediaPlayer
//    }
//
//    private fun initListeners(createRetrofit:ApiService) {
//        binding.btnGenerateAudio.setOnClickListener {
//            textToSpeech(binding.etSpeech.text.toString(),createRetrofit)
//            hideKeyboard(this)
//        }
//
//        binding.btnPlayAudio.setOnClickListener{
//            getGif()
//            playAudio()
//        }
//
//    }
//
//    fun hideKeyboard(activity: Activity) {
//        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        val currentFocusView = activity.currentFocus
//
//        if (currentFocusView != null) {
//            inputMethodManager.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
//        }
//    }
//
//    private fun playAudio() {
//        if(starStopVoice){
//            binding.btnPlayAudio.setImageResource(R.drawable.ic_stop_24)
//            binding.ivAvatar.isVisible = true
//            mediaPlayer.setDataSource(pathVoice)
//            mediaPlayer.prepare()
//            mediaPlayer.start()
//            starStopVoice = false
//            mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
//                override fun onCompletion(mp: MediaPlayer?) {
//                    starStopVoice = true
//                    binding.btnPlayAudio.setImageResource(R.drawable.ic_play_24)
//                    binding.ivAvatar.isVisible = false
//                    mediaPlayer.release()
//                    mediaPlayer=MediaPlayer()
//                }
//            })
//
//        }else{
//            starStopVoice = true
//            binding.btnPlayAudio.setImageResource(R.drawable.ic_play_24)
//            binding.ivAvatar.isVisible = false
//            mediaPlayer.stop()
//            mediaPlayer.release()
//            mediaPlayer=MediaPlayer()
//        }
//    }
//
//    private fun textToSpeech(speechText:String,createRetrofit:ApiService){
//        lateinit var callMp3: ResponseBody
//        val context = this
//        binding.progressBar.isVisible = true
//        lifecycleScope.launch(Dispatchers.IO) {
//            getVoiceId().collect{voiceId ->
//                callMp3= createRetrofit.textToSpeech(
//                    voiceId, 0, "mp3_44100_128", API_KEY_VOICE_SERVICE,
//                    TextToSpechDto(speechText,"eleven_monolingual_v1", VoiceSettingsDto(0, 0, 0, true))
//                )
//                pathVoice = callMp3.saveFile(context)
//                Log.i("mp3", callMp3.toString())
//                runOnUiThread {
//                    binding.progressBar.isVisible = false
//                }
//            }
//        }
//    }
//
//    private fun getGif(){
//        //"https://i.gifer.com/origin/dc/dc99eb039f75ee476a5ebbb0529c4529_w200.gif"
//        Glide.with(this)
//            .asGif()
//            .load(R.raw.newgif)
//            .into(binding.ivAvatar)
//    }
//
//    private fun getAllVoices(createRetrofit:ApiService) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            val myResponse: Response<AllVoicesDataResponse> = createRetrofit.getAllVoices(API_KEY_VOICE_SERVICE)
//            if (myResponse.isSuccessful) {
//                Log.i("elevenlabs", "funciona :)")
//                val response: AllVoicesDataResponse? = myResponse.body()
//                Log.i("elevenlabs", response.toString())
//                response?.voices?.let {
//                    runOnUiThread {
//                        //Mostar la lista de voces
//                        setupVoicesSpinner(it)
//                    }
//                }
//            } else {
//                Log.i("elevenlabs", "No funciona :(")
//            }
//        }
//    }
//
//    private fun setupVoicesSpinner(voices: List<VoiceItemResponse>) {
//        val context: Context = this
//        with(binding) {
//            val items= voices.map{voice -> voice.name}
//            val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1,items)
//            spSelectVoice.setAdapter(adapter)
//            spSelectVoice.onItemClickListener = android.widget.AdapterView.OnItemClickListener{ adapterView, view, i, l ->
//                val itemSelected = adapterView.getItemAtPosition(i)
//                Log.i("brian", "seleccion numero $i")
//                lifecycleScope.launch(Dispatchers.IO) {
//                    saveVoiceId(voices[i])
//                }
//            }
//        }
//    }
//
//    private suspend fun saveVoiceId( voiceItemResponse: VoiceItemResponse){
//        dataStore.edit {preferences ->
//            preferences[stringPreferencesKey(VOICE_ID)] = voiceItemResponse.voiceId
//        }
//    }
//
//    private fun getVoiceId() = dataStore.data.map { preferences ->
//        preferences[stringPreferencesKey(VOICE_ID)].orEmpty()
//    }

}

//private fun ResponseBody.saveFile(context: Context) : String {
//    val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "voice.mp3")
//    byteStream().use { inputStream->
//        destinationFile.outputStream().use { outputStream->
//            inputStream.copyTo(outputStream)
//        }
//    }
//
//    return destinationFile.path
//}