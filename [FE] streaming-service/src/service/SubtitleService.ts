// subtitle.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SubtitleService {
  constructor(private http: HttpClient) {}

  getSubtitles(videoId: string, languageCode: string): Observable<string> {
    return this.http.get(`http://68.10.187.106:4200/subtitles/${videoId}/${languageCode}`, { responseType: 'text' });
  }

  getAvailableSubtitles(videoId: string): Observable<string[]> {
    return this.http.get<string[]>(`http://68.10.187.106:4200/subtitles/${videoId}/languages`);
  }
}
