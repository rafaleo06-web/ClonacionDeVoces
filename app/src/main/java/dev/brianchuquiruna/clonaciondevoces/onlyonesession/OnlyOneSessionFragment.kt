package dev.brianchuquiruna.clonaciondevoces.onlyonesession

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_PARENTS
import dev.brianchuquiruna.clonaciondevoces.FIREBASE_RECORD_VOICE
import dev.brianchuquiruna.clonaciondevoces.GetFirebase
import dev.brianchuquiruna.clonaciondevoces.MainActivity
import dev.brianchuquiruna.clonaciondevoces.R
import dev.brianchuquiruna.clonaciondevoces.databinding.FragmentOnlyOneSessionBinding
import dev.brianchuquiruna.clonaciondevoces.util.PerfilActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class OnlyOneSessionFragment : Fragment() {

    private lateinit var binding: FragmentOnlyOneSessionBinding
    private lateinit var actividad: MainActivity
    private lateinit var adapter: OnlyOneSessionAdapter
    private var mediaPlayer = MediaPlayer()
    private val args:OnlyOneSessionFragmentArgs by navArgs()
    private lateinit var perfil:PerfilActive
    private var currentVoiceIdSelected:String = ""
    private val recordVoiceListResponse = RecordVoiceListResponse(mutableListOf<RecordVoiceResponse>())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOnlyOneSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPerfilActive()
        actividad = activity as MainActivity
        initUI()
        initListeners()
    }

    private fun checkPerfilActive() {
        lifecycleScope.launch(Dispatchers.IO) {
            actividad.getPerfilActive().collect(){
                perfil = it
            }
        }
    }

    private fun initListeners() {
        binding.btnAudio.setOnClickListener {
            findNavController().navigate(OnlyOneSessionFragmentDirections.actionOnlyOneSessionFragmentToCloneVoiceFragment(email = args.email, idSession = args.idSession))
        }

        binding.ivAvatar.btnClose.setOnClickListener {
            binding.ivAvatar.itemImageAudio.isGone = true
        }

        binding.ivAvatar.btnPlay.setOnClickListener {
            recordVoiceListResponse.response.find { recordVoiceResponse ->
                recordVoiceResponse.id == currentVoiceIdSelected
            }?.let { it1 -> playAudioRecord(it1,binding.ivAvatar.btnPlay) }
        }

        binding.ivAvatar.btnRight.setOnClickListener {
            recordVoiceListResponse.response.find { recordVoiceResponse ->
                recordVoiceResponse.id == currentVoiceIdSelected
            }?.let { it1 ->
                when (val index = recordVoiceListResponse.response.indexOf(it1)) {
                    in 0 until recordVoiceListResponse.response.size-1 -> {
                        currentVoiceIdSelected = recordVoiceListResponse.response[index+1].id
                    }
                    recordVoiceListResponse.response.size-1 -> {
                        currentVoiceIdSelected = recordVoiceListResponse.response[0].id
                    }
                }
            }
        }

        binding.ivAvatar.btnLeft.setOnClickListener {
            recordVoiceListResponse.response.find { recordVoiceResponse ->
                recordVoiceResponse.id == currentVoiceIdSelected
            }?.let { it1 ->
                when (val index = recordVoiceListResponse.response.indexOf(it1)) {
                    0 -> {
                        currentVoiceIdSelected = recordVoiceListResponse.response[recordVoiceListResponse.response.size-1].id
                    }

                    in 1 until recordVoiceListResponse.response.size -> {
                        currentVoiceIdSelected = recordVoiceListResponse.response[index-1].id
                    }
                }
            }
        }


    }

    private fun getAllRecords() {
        binding.progressBar.isGone = false
        lifecycleScope.launch(Dispatchers.IO) {
            GetFirebase.db.collection(FIREBASE_PARENTS).document(args.email).collection("topicsLearned").document(args.idSession).collection(FIREBASE_RECORD_VOICE).get()
                .addOnSuccessListener { it ->
                    recordVoiceListResponse.response.clear()
                    for (iterator in it.documents) {
                        iterator?.getString("title")?.let { title->
                            Log.i("RecordVoice", title)
                            iterator?.getString("urlRecord")?.let { urlRecord->
                                Log.i("RecordVoice", urlRecord)
                                iterator?.getDate("date")?.let { date ->
                                    Log.i("RecordVoice", date.toString())
                                    recordVoiceListResponse.response.add(RecordVoiceResponse(iterator.id,title, urlRecord,date))
                                }
                            }
                        }
                    }
                    recordVoiceListResponse.response.sortBy { voiceResponse -> voiceResponse.date }
                    actividad.runOnUiThread {
                        adapter.updateList(recordVoiceListResponse.response.toList())
                        binding.progressBar.isGone = true
                    }
                }.addOnFailureListener { exception ->
                    Log.i("FIREBASE_ERROR","Error al recuperar las grabaciones de audio de Firebase: $exception")
                }
        }
    }

    private fun setAudioRecordAdapter() {
        adapter = OnlyOneSessionAdapter { recordVoiceResponse,ivPlay ->
            if(perfil == PerfilActive.STUDENT){
                binding.ivAvatar.itemImageAudio.isGone = false
            }
            playAudioRecord(recordVoiceResponse,ivPlay)
            currentVoiceIdSelected = recordVoiceResponse.id
        }
        binding.rvAllAudios.setHasFixedSize(true)
        binding.rvAllAudios.layoutManager = LinearLayoutManager(context)
        binding.rvAllAudios.adapter = adapter
    }

    private fun playAudioRecord(recordVoiceResponse: RecordVoiceResponse, ivPlay: ImageView) {
        var imagePlay = R.drawable.ic_play_24
        var imageStop = R.drawable.ic_stop_24
        if(perfil==PerfilActive.STUDENT){
            imagePlay = R.drawable.ic_play_24_version_2
            imageStop = R.drawable.ic_stop_24_version_2
        }

        mediaPlayer.setOnCompletionListener(object : MediaPlayer.OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer?) {
                ivPlay.setImageResource(imagePlay)
            }
        })

        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            ivPlay.setImageResource(imagePlay)
        }else{
            mediaPlayer.reset()
            mediaPlayer.setDataSource(recordVoiceResponse.urlRecord)
            mediaPlayer.prepare()
            mediaPlayer.start()
            ivPlay.setImageResource(imageStop)
        }
    }

    private fun initUI() {
        binding.tvTitle.text = args.titleSesion
        setAudioRecordAdapter()
        getAllRecords()
        getGif()
        if(perfil == PerfilActive.STUDENT) {
            binding.btnAudio.isGone = true
        }
    }

    private fun getGif(){
        val currentFragment = this
        lifecycleScope.launch(Dispatchers.IO) {
            GetFirebase.db.collection(FIREBASE_PARENTS).document(args.email).get()
                .addOnSuccessListener { parent ->
                    parent.getString("image")?.let {image ->
                        Glide.with(currentFragment)
                            .load(image)
                            .into(binding.ivAvatar.ivAvatar)
                    }
                }.addOnFailureListener { exception ->
                    // Maneja errores si la consulta falla
                    Glide.with(currentFragment)
                        .asGif()
                        .load(R.raw.newgif)
                        .into(binding.ivAvatar.ivAvatar)
                    println("Error al recuperar la imagen de Firebase: $exception")
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying){
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}