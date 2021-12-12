import {HttpClient, HttpEventType} from "@angular/common/http";
import {Component, Input} from "@angular/core";
import {Subscription} from "rxjs";
import {finalize} from "rxjs/operators";
import { UserService } from "src/app/services";

@Component({
  selector: 'app-file-upload',
  templateUrl: "file-upload.component.html",
  styleUrls: ["file-upload.component.css"]
})
export class FileUploadComponent {
  @Input()
  requiredFileType:string;

  fileName = '';
  uploadProgress:number;
  uploadSub: Subscription;

  constructor(private userService: UserService) {}

  onFileSelected(event) {
    const file:File = event.target.files[0];

    if (file) {
      this.fileName = file.name;
      const upload$ = this.userService.upload(file)
        .pipe(
          finalize(() => this.reset())
        );

      this.uploadSub = upload$.subscribe(event => {
        if (event.type == HttpEventType.UploadProgress) {
          this.uploadProgress = Math.round(100 * (event.loaded / event.total));
        }
      })
    }
  }

  cancelUpload() {
    this.uploadSub.unsubscribe();
    this.reset();
  }

  reset() {
    this.uploadProgress = null;
    this.uploadSub = null;
  }
}
