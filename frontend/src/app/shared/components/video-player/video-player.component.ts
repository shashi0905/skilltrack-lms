import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CourseService } from '../../../core/services/course.service';

declare var Hls: any;

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="video-player-container">
      <div class="video-wrapper" [class.loading]="isLoading">
        <video 
          #videoElement
          class="video-player"
          [poster]="posterUrl"
          controls
          preload="metadata"
          controlsList="nodownload"
          oncontextmenu="return false;"
          [style.display]="isLoading ? 'none' : 'block'">
          Your browser does not support the video tag.
        </video>
        
        <div class="loading-overlay" *ngIf="isLoading">
          <div class="loading-spinner">
            <i class="fas fa-spinner fa-spin"></i>
            <p>Loading video...</p>
          </div>
        </div>

        <div class="error-overlay" *ngIf="hasError">
          <div class="error-message">
            <i class="fas fa-exclamation-triangle"></i>
            <h4>Video Unavailable</h4>
            <p>{{ errorMessage }}</p>
            <button class="btn btn-primary" (click)="retryLoad()">
              <i class="fas fa-redo"></i> Retry
            </button>
          </div>
        </div>

        <!-- Watermark overlay -->
        <div class="watermark" *ngIf="watermarkText">
          {{ watermarkText }}
        </div>

        <!-- Custom controls overlay for additional security -->
        <div class="custom-controls" *ngIf="!isLoading && !hasError">
          <div class="playback-info">
            <span class="current-time">{{ formatTime(currentTime) }}</span>
            <span class="separator">/</span>
            <span class="duration">{{ formatTime(duration) }}</span>
          </div>
          <div class="quality-selector" *ngIf="availableQualities.length > 1">
            <select (change)="changeQuality($event)" [value]="currentQuality">
              <option *ngFor="let quality of availableQualities" [value]="quality.height">
                {{ quality.height }}p
              </option>
            </select>
          </div>
        </div>
      </div>

      <div class="video-info" *ngIf="showInfo">
        <div class="video-details">
          <h5>{{ videoTitle }}</h5>
          <p *ngIf="videoDescription">{{ videoDescription }}</p>
          <div class="video-meta">
            <span *ngIf="videoDuration">Duration: {{ formatTime(videoDuration) }}</span>
            <span *ngIf="videoSize">Size: {{ videoSize }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .video-player-container {
      width: 100%;
      max-width: 100%;
      background: #000;
      border-radius: 8px;
      overflow: hidden;
      position: relative;
    }

    .video-wrapper {
      position: relative;
      width: 100%;
      padding-bottom: 56.25%; /* 16:9 aspect ratio */
      height: 0;
      background: #000;
    }

    .video-player {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: #000;
    }

    .loading-overlay,
    .error-overlay {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: rgba(0, 0, 0, 0.8);
      color: white;
    }

    .loading-spinner,
    .error-message {
      text-align: center;
    }

    .loading-spinner i {
      font-size: 2rem;
      margin-bottom: 10px;
    }

    .error-message i {
      font-size: 3rem;
      color: #dc3545;
      margin-bottom: 15px;
    }

    .watermark {
      position: absolute;
      top: 10px;
      left: 10px;
      background: rgba(0, 0, 0, 0.7);
      color: white;
      padding: 5px 10px;
      border-radius: 4px;
      font-size: 12px;
      pointer-events: none;
      z-index: 10;
    }

    .custom-controls {
      position: absolute;
      bottom: 10px;
      right: 10px;
      display: flex;
      gap: 15px;
      align-items: center;
      background: rgba(0, 0, 0, 0.7);
      padding: 8px 12px;
      border-radius: 4px;
      color: white;
      font-size: 12px;
    }

    .playback-info {
      display: flex;
      align-items: center;
      gap: 5px;
    }

    .quality-selector select {
      background: transparent;
      border: 1px solid rgba(255, 255, 255, 0.3);
      color: white;
      padding: 2px 5px;
      border-radius: 3px;
      font-size: 11px;
    }

    .video-info {
      padding: 15px;
      background: #f8f9fa;
      border-top: 1px solid #dee2e6;
    }

    .video-meta {
      display: flex;
      gap: 20px;
      margin-top: 10px;
      font-size: 14px;
      color: #6c757d;
    }

    /* Disable right-click and text selection */
    .video-player-container {
      -webkit-user-select: none;
      -moz-user-select: none;
      -ms-user-select: none;
      user-select: none;
    }

    /* Hide download button in Chrome */
    .video-player::-webkit-media-controls-download-button {
      display: none;
    }

    .video-player::-webkit-media-controls-enclosure {
      overflow: hidden;
    }

    .video-player::-webkit-media-controls-panel {
      width: calc(100% + 30px);
      margin-left: -15px;
    }
  `]
})
export class VideoPlayerComponent implements OnInit, OnDestroy, AfterViewInit {
  @Input() mediaAssetId!: string;
  @Input() videoTitle?: string;
  @Input() videoDescription?: string;
  @Input() videoDuration?: number;
  @Input() videoSize?: string;
  @Input() watermarkText?: string;
  @Input() posterUrl?: string;
  @Input() showInfo = true;
  @Input() autoplay = false;

  @ViewChild('videoElement', { static: true }) videoElement!: ElementRef<HTMLVideoElement>;

  private hls: any;
  isLoading = true;
  hasError = false;
  errorMessage = '';
  currentTime = 0;
  duration = 0;
  currentQuality = 'auto';
  availableQualities: any[] = [];

  constructor(private courseService: CourseService) {}

  ngOnInit() {
    this.loadHlsLibrary();
  }

  ngAfterViewInit() {
    this.setupVideoEventListeners();
  }

  ngOnDestroy() {
    this.destroyHls();
  }

  private loadHlsLibrary() {
    if (typeof Hls !== 'undefined') {
      this.initializePlayer();
      return;
    }

    // Load HLS.js library dynamically
    const script = document.createElement('script');
    script.src = 'https://cdn.jsdelivr.net/npm/hls.js@latest';
    script.onload = () => {
      this.initializePlayer();
    };
    script.onerror = () => {
      this.handleError('Failed to load video player library');
    };
    document.head.appendChild(script);
  }

  private initializePlayer() {
    if (!this.mediaAssetId) {
      this.handleError('No video source provided');
      return;
    }

    // Get signed URL for video streaming
    this.courseService.getVideoStreamUrl(this.mediaAssetId).subscribe({
      next: (streamUrl) => {
        this.loadVideo(streamUrl);
      },
      error: (error) => {
        this.handleError('Failed to get video stream URL');
      }
    });
  }

  private loadVideo(streamUrl: string) {
    const video = this.videoElement.nativeElement;

    if (Hls.isSupported()) {
      this.hls = new Hls({
        enableWorker: true,
        lowLatencyMode: false,
        backBufferLength: 90
      });

      this.hls.loadSource(streamUrl);
      this.hls.attachMedia(video);

      this.hls.on(Hls.Events.MANIFEST_PARSED, () => {
        this.isLoading = false;
        this.hasError = false;
        this.extractQualityLevels();
        
        if (this.autoplay) {
          video.play().catch(e => {
            console.warn('Autoplay failed:', e);
          });
        }
      });

      this.hls.on(Hls.Events.ERROR, (event: any, data: any) => {
        if (data.fatal) {
          this.handleError('Video streaming error: ' + data.details);
        }
      });

    } else if (video.canPlayType('application/vnd.apple.mpegurl')) {
      // Native HLS support (Safari)
      video.src = streamUrl;
      video.addEventListener('loadedmetadata', () => {
        this.isLoading = false;
        this.hasError = false;
      });
      video.addEventListener('error', () => {
        this.handleError('Video playback error');
      });
    } else {
      this.handleError('HLS is not supported in this browser');
    }
  }

  private setupVideoEventListeners() {
    const video = this.videoElement.nativeElement;

    video.addEventListener('timeupdate', () => {
      this.currentTime = video.currentTime;
    });

    video.addEventListener('loadedmetadata', () => {
      this.duration = video.duration;
    });

    video.addEventListener('error', () => {
      this.handleError('Video playback error');
    });

    // Prevent right-click context menu
    video.addEventListener('contextmenu', (e) => {
      e.preventDefault();
      return false;
    });

    // Prevent keyboard shortcuts for downloading
    video.addEventListener('keydown', (e) => {
      if (e.ctrlKey && (e.key === 's' || e.key === 'a')) {
        e.preventDefault();
      }
    });
  }

  private extractQualityLevels() {
    if (this.hls && this.hls.levels) {
      this.availableQualities = this.hls.levels.map((level: any, index: number) => ({
        index,
        height: level.height,
        bitrate: level.bitrate
      }));
    }
  }

  changeQuality(event: any) {
    const selectedHeight = parseInt(event.target.value);
    if (this.hls) {
      const levelIndex = this.availableQualities.findIndex(q => q.height === selectedHeight);
      if (levelIndex >= 0) {
        this.hls.currentLevel = levelIndex;
        this.currentQuality = selectedHeight.toString();
      }
    }
  }

  retryLoad() {
    this.hasError = false;
    this.isLoading = true;
    this.destroyHls();
    this.initializePlayer();
  }

  private handleError(message: string) {
    this.isLoading = false;
    this.hasError = true;
    this.errorMessage = message;
    console.error('Video Player Error:', message);
  }

  private destroyHls() {
    if (this.hls) {
      this.hls.destroy();
      this.hls = null;
    }
  }

  formatTime(seconds: number): string {
    if (!seconds || isNaN(seconds)) return '0:00';
    
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);

    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    } else {
      return `${minutes}:${secs.toString().padStart(2, '0')}`;
    }
  }
}