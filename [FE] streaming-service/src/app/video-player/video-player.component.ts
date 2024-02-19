import { Component  ,OnInit,ElementRef,ViewChild,Renderer2, Input} from '@angular/core';
import videojs from 'video.js';
import { SubtitleService } from 'src/service/SubtitleService';
import { AudioService } from 'src/service/AudioService';

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
          receiverAppID: '1234', // Not required
          addButtonToControlBar: true, // Defaults to true
          // code for receiver app ID etc goes here
        },
      },
      chromecast: {
        requestTitleFn: () => {
          return 'title';
        },
        requestSubtitleFn: () => {
          return 'subtitle';
        },
        requestCustomDataFn: () => {
          // project specific things
        },
      },
      
  };

  player:any

  constructor(private subtitleService: SubtitleService, private audioService:AudioService,private renderer: Renderer2) {
   }


  videoId = "1"
  defaultAudioTrack=""
  videoUrl = "http://www.localhost:8080/videos/"
  audioTracks:string[]=[];
  subtitleLoaded:boolean=false;
  ngAfterViewInit(): void {
    this.player = videojs(this.videoPlayer.nativeElement,this.options);
    this.initPlayer()

    setTimeout(() => {
      let chromecastButton = document.querySelector(".vjs-chromecast-button")?.classList.remove('vjs-hidden')
        
    }, 1000); // waits 1 second before running the code inside the timeout
  }

  ngOnInit() {
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
    
    // Ensure the player element, defaultAudioTrack, and videoId are available
    if (this.videoPlayer && this.defaultAudioTrack && this.videoId) {
      
      // Set up player source with the default audio track
      this.player.src({
        src: `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`,
        type: 'video/mp4',
      });
      this.player.load();
      console.log(this.player)
      this.loadSubtitlesToPlayer(this.player);
    }
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
    console.log("call A")
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
