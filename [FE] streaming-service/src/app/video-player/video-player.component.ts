import { Component  ,OnInit,ElementRef,ViewChild, Input} from '@angular/core';
import videojs from 'video.js';
import { SubtitleService } from 'src/service/SubtitleService';
import { AudioService } from 'src/service/AudioService';

@Component({
  selector: 'app-video-player',
  templateUrl: './video-player.component.html',
  styleUrls: ['./video-player.component.css']
})
export class VideoPlayerComponent implements OnInit   {

  @ViewChild('videoPlayer') videoPlayer!: ElementRef;

  constructor(private subtitleService: SubtitleService, private audioService:AudioService) {
   }


  videoId = "1"
  defaultAudioTrack=""
  videoUrl = "http://www.localhost:8080/videos/"
  audioTracks:string[]=[];
  subtitleLoaded:boolean=false;
  ngAfterViewInit(): void {
    this.initPlayer()
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
      const player = videojs(this.videoPlayer.nativeElement);
      // Set up player source with the default audio track
      player.src({
        src: `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`,
        type: 'video/mp4',
      });
      player.load();
      console.log("call B")

      this.loadSubtitlesToPlayer(player);
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

    const player = videojs(this.videoPlayer.nativeElement);
    player.src({
      src: `${this.videoUrl}${this.videoId}_${this.defaultAudioTrack}`,
      type: 'video/mp4',
    });
    console.log("call A")
    this.loadSubtitlesToPlayer(player);
    player.play();
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
