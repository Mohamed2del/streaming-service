import { Component  ,OnInit,ElementRef,ViewChild} from '@angular/core';
import videojs from 'video.js';
import { SubtitleService } from 'src/service/SubtitleService';
import { AudioService } from 'src/service/AudioService';
import { ActivatedRoute } from '@angular/router'; 

require('@silvermine/videojs-chromecast')(videojs); // THIS FAILS IN ANGULAR 13.X, but not 12.X
import '@silvermine/videojs-chromecast/dist/silvermine-videojs-chromecast.css';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.css']
})
export class VideoPlayerComponent implements OnInit   {

  @ViewChild('videoPlayer') videoPlayer!: ElementRef;

  options = {
    liveui: true,
    techOrder: ['chromecast', 'html5'],
      plugins: { // UNCOMMENT THIS BLOCK FOR CHROMECAST
        chromecast: {
          addButtonToControlBar: true, 
        },
      },
      chromecast: {
        requestTitleFn: () => {
          return this.videoId;
        },
        requestSubtitleFn: () => {
          return this.defaultAudioTrack;
        },
        requestCustomDataFn: () => {
          return `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`
        },
      },
      
  };

  player:any
  videoLoadError = false;

  constructor(private subtitleService: SubtitleService, 
    private audioService:AudioService,  
      private route: ActivatedRoute) {
   }

  
  videoId:string="";
  defaultAudioTrack=""
  videoUrl = "http://www.localhost:8080/videos/"
  audioTracks:string[]=[];
  subtitleLoaded:boolean=false;
  ngAfterViewInit(): void {
    this.player = videojs(this.videoPlayer.nativeElement,this.options);
    this.initPlayer()

    setTimeout(() => {
    document.querySelector(".vjs-chromecast-button")?.classList.remove('vjs-hidden') 
    }, 1000); // waits 1 second before running the code inside the timeout
  const videoElement: HTMLVideoElement = this.videoPlayer.nativeElement;
  videoElement.addEventListener('error', this.handleVideoError.bind(this));
  videoElement.src = 'invalid-video-url';
  }

  ngOnInit() {
    this.route.paramMap.subscribe(params => {
      const videoIdParam = params.get('videoId');
    if (videoIdParam !== null) {
      this.videoId = videoIdParam;
    }
    });
  console.log("videoId"+this.videoId)
  this.loadAudioToPlayer();
}
  
  loadAudioToPlayer(): void {
    // Ensure videoId is not null
    if (this.videoId) {
      this.audioService.fetchAudioTracks(this.videoId).subscribe(
        (tracks) => {
          this.audioTracks = tracks;
          if (tracks.length > 0) {
            this.defaultAudioTrack = tracks[0]; // Set the first track as default
            this.initPlayer(); // Initialize the player now that we have the default track
          }
        },
        (error) => {
          console.error('Error fetching audio tracks', error);
        }
      );
    }
  }
  
  initPlayer(): void {
    if (this.videoPlayer && this.defaultAudioTrack && this.videoId) {
      this.player.src({
        src: `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`,
        type: 'video/mp4',
      });
      this.player.load();
      this.player.chromecast();
      this.loadSubtitlesToPlayer(this.player);
    }
  }
  
handleVideoError(event: Event): void {
  console.error('Error loading video:', event);
  this.videoLoadError = true;
}

  changeAudioTrack(event: Event): void {
    const selectElement = event.target as HTMLSelectElement;

    if (selectElement && selectElement.value) {
      const selectedValue = selectElement.value;
      console.log(selectedValue); 
  

      this.defaultAudioTrack = selectedValue;
  
    } else {
      console.warn('No audio track selected');
    }

    this.player.src({
      src: `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`,
      type: 'video/mp4',
    });
    this.loadSubtitlesToPlayer(this.player);
    this.player.play();
  }

  loadSubtitlesToPlayer(player:any) : void{
    this.subtitleService.getAvailableSubtitles(this.videoId).subscribe(languageCodes=>{
      console.log(languageCodes)
      languageCodes.forEach(code => {
        this.subtitleService.getSubtitles(this.videoId, code).subscribe(subtitleText => {
          const blob = new Blob([subtitleText], { type: 'text/vtt;charset=utf-8' });
          const subtitleUrl = URL.createObjectURL(blob);
          console.log(blob)
          player.addRemoteTextTrack({
            kind: 'subtitles',
            src: subtitleUrl,
            srclang: code,
            label: code.toUpperCase(),
          }, false);
        });
      });
    }); 
  
}
}
