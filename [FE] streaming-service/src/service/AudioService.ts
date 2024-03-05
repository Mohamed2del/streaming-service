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
    return this.http.get<string[]>(`http://68.10.187.106:8123/audio/${videoId}/languages`);
  }

}
