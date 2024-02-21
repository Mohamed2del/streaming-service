import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent {
  videoId: string = '';

  constructor(private router: Router) {}

  navigateToVideo(): void {
    if (this.videoId.trim() !== '') {
      console.log(this.videoId)
      this.router.navigate(['/videos', this.videoId]);
    }
  }
}
