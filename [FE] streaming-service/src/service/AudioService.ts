// subtitle.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
@Injectable({
  providedIn: 'root'
})
export class AudioService {
  constructor(private http: HttpClient) {}

  fetchAudioTracks(videoId: string): Observable<string[]> {
    return this.http.get<string[]>(`http://localhost:8080/audio/${videoId}/languages`);
  }

}
