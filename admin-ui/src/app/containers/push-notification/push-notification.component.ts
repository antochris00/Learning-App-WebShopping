import { Component, OnInit } from '@angular/core';
import { NotificationService } from '../../shared/notification/notification.service';
import { AlertService } from '../../shared/_alert';
import { CookieService } from 'ngx-cookie-service';
import { LoginService } from '../../service/login/login.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-push-notification',
  templateUrl: './push-notification.component.html',
  styleUrls: ['./push-notification.component.css']
})
export class PushNotificationComponent implements OnInit {

  notifications:any[] = new Array();
  loading = true;
  notificationCount = 0;

  constructor(private notificationService: NotificationService,
              private alertService: AlertService,
              private cookieService: CookieService,
              private loginService: LoginService,
              private _snackBar: MatSnackBar) { }

  ngOnInit(): void {
    let allowCall =this.cookieService.get('JWT');
    let notificationInterval;
    let loggedStatusInterval;
    if(allowCall != null && allowCall != undefined && allowCall != ''){
        this.getNotifications();
        notificationInterval = setInterval(() => { this.getNotifications() }, 60000);
       //need to moved to related method later.
       loggedStatusInterval = setInterval(() => { this.updateLoggedInStatus() }, 50000);

    }
    else{
      clearInterval(notificationInterval);
      clearInterval(loggedStatusInterval);
    }
  }

  getNotifications(){
    this.notificationService.getAllNotifications()
                            .subscribe((resp:any) => {
                              if(resp.statusCode === 200){
                                this.notifications = resp.dataList;
                                if(this.notificationCount !== this.notifications.length){
                                  this._snackBar.open('You Have a New Notification(s)!', '', {
                                    duration: 5000,
                                    panelClass: ['warn-snackbar']
                                  });
                                }
                                this.notificationCount = this.notifications.length;
                              }
                            });
  }

  clearNotification(notification:any){
    this.notificationService.deleteNotification(notification.notificationId)
                            .subscribe((resp:any) => {
                              if(resp.statusCode === 200){
                                this.notifications = resp.dataList;
                                this.notificationCount -= 1;
                              }
                            });
  }

  updateLoggedInStatus(){
    this.loginService.updateLoginStatus()
                      .subscribe((resp:any) => {
                        if(resp.statusCode === 200){
                          this.notifications = resp.dataList;
                        }
                      });
  }

}
