/*
 * Copyright 2022 Pera Wallet, LDA
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package network.voi.hera.nft.ui.mediaplayer

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import network.voi.hera.MainActivity
import network.voi.hera.R
import network.voi.hera.core.BaseFragment
import network.voi.hera.databinding.FragmentMediaPlayerBinding
import network.voi.hera.models.FragmentConfiguration
import network.voi.hera.utils.setScreenOrientationFullSensor
import network.voi.hera.utils.setScreenOrientationPortrait
import network.voi.hera.utils.viewbinding.viewBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource

abstract class MediaPlayerFragment : BaseFragment(R.layout.fragment_media_player) {

    override val fragmentConfiguration = FragmentConfiguration()

    protected val binding by viewBinding(FragmentMediaPlayerBinding::bind)

    abstract val mediaPlayerViewModel: MediaPlayerViewModel

    private var exoPlayer: ExoPlayer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    protected open fun initUi() {
        binding.backButton.setOnClickListener { navBack() }
        loadMedia()
    }

    private fun loadMedia() {
        val videoUrl = mediaPlayerViewModel.collectibleMediaUrl
        val mediaSource = createMediaSource(videoUrl)
        setupPlayer(mediaSource)
    }

    override fun onResume() {
        super.onResume()
        activity?.setScreenOrientationFullSensor()
        resumePlayer()
    }

    override fun onPause() {
        super.onPause()
        activity?.setScreenOrientationPortrait()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyExoPlayer()
    }

    private fun resumePlayer() {
        (activity as? MainActivity)?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        exoPlayer?.playWhenReady = true
    }

    private fun pausePlayer() {
        (activity as? MainActivity)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        exoPlayer?.playWhenReady = false
    }

    private fun destroyExoPlayer() {
        exoPlayer?.release()
    }

    private fun setupPlayer(mediaSource: MediaSource?) {
        if (mediaSource != null) {
            exoPlayer = ExoPlayer.Builder(binding.root.context).build().apply {
                binding.playerView.player = this
                setMediaSource(mediaSource)
                playWhenReady = true
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        }
    }

    private fun createMediaSource(url: String): MediaSource {
        val uri = Uri.parse(url)
        val progressiveMediaSource = ProgressiveMediaSource.Factory(buildDataSourceFactory())
        return progressiveMediaSource.createMediaSource(MediaItem.fromUri(uri))
    }

    private fun buildDefaultBandwidthMeter(): DefaultBandwidthMeter {
        return DefaultBandwidthMeter.Builder(binding.root.context).build()
    }

    private fun buildDataSourceFactory(): DataSource.Factory {
        val httpDataSource = DefaultHttpDataSource.Factory()
        return DefaultDataSource.Factory(binding.root.context, httpDataSource).apply {
            setTransferListener(buildDefaultBandwidthMeter())
        }
    }
}
