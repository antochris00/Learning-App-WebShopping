import {CommonsService} from '../../service/shared/commons/commons.service';
import {SocialUser, FacebookLoginProvider,  GoogleLoginProvider,  SocialAuthService} from 'angularx-social-login';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';
import { CartService } from 'src/app/service/cart/cart.service';
import { LoginService } from 'src/app/service/login/login.service';
import { TenantStoreService } from 'src/app/service/shared/tenant-store/tenant-store.service';
import { UserStoreService } from 'src/app/service/shared/user-store/user-store.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { LoginDialogComponent } from './login-dialog/login-dialog.component';
import { FeatureService } from 'src/app/service/feature/feature.service';

declare var rsaencrypt: Function;

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  googlereDirectUrl:string;
  loading = false;
  registerLoding = false;
  signInLoading = false;
  buttonLoading = false;
  buttonDisable = false;

  emailId:string;
  password:string;
  otp:string;
  otpnumber: number;
  fName:string;
  lName:string;
  mobile:string;
  rememberMe:boolean = false;

  socialUser: SocialUser;
  loggedIn: boolean;

  allowMobileRegister = false;
  allowMobileRegisterFeatureName = "RegisterWithMobile";

  constructor(private loginService: LoginService, private tenantStore: TenantStoreService,
              private route: Router, private cookieService: CookieService,
              private userStore: UserStoreService, private cartService: CartService,
              private authService: SocialAuthService, public dialog: MatDialog,
              public commonService: CommonsService, private _snackBar: MatSnackBar,
              private featureService: FeatureService) { }

  ngOnInit(): void {
    if(this.userStore.emailId !== undefined && this.userStore.emailId !== null){
      this.route.navigate(["/home"]);
    }
    this.authService.authState.subscribe((user) => {
      this.loading = true;
      this.socialUser = user;
      this.loginService.googleSocialLogin(this.socialUser)
                        .subscribe((resp:any) => {
                          if(resp.statusCode === 200){
                              this.userStore.JwtToken = resp.data;
                              this.userStore.active=resp.dataList[0].active;
                              this.userStore.emailId=resp.dataList[0].emailId;
                              this.userStore.customerAddress=resp.dataList[0].customerAddress;
                              this.userStore.userId=resp.dataList[0].customerId;
                              this.userStore.firstName=resp.dataList[0].firstName;
                              this.userStore.lastName=resp.dataList[0].lastName;
                              this.userStore.mobile=resp.dataList[0].mobile;
                              if(this.userStore.mobile === null || this.userStore.mobile === undefined){
                                this._snackBar.open('Please Update Mobile Number under Profile to Earn Loyality points!', '', this.commonService.alertoptionsWarn);
                              }
                              this.userStore.loyalityPoints=resp.dataList[0].loyalitypoint;
                              this.userStore.lastLogin=resp.dataList[0].lastLogin;
                              this.userStore.profilePic=resp.dataList[0].profilePic;
                              this.userStore.profilePicUrl=resp.dataList[0].profilePicUrl;
                              this.userStore.loginMode=resp.dataList[0].loginMode;

                              this.cookieService.deleteAll();
                              this.cookieService.set('CLIENTJWT', this.userStore.JwtToken, 90);

                              this.setCartItems();

                              this.route.navigate(["/home"]);
                            }
                            else{
                              alert('Failed : ' + resp.errorMessages);
                            }
                            this.signInLoading = false;
                            this.loading = false;
                          },
                          (error:any) => {
                            alert('Something went wrong!');
                            this.loading = false;
                          });
                      });
        this.featureService.getFeatureStatus(this.allowMobileRegisterFeatureName)
                            .subscribe((resp:any) => {
                              if(resp.statusCode === 200){
                                this.allowMobileRegister = resp.data.featureStatus;
                              }
                            },
                            (error:any) => {
                              alert('Something went wrong!');
                            });
  }

  signInWithGoogle(): void {
    this.authService.signIn(GoogleLoginProvider.PROVIDER_ID);
  }

  signInWithFB(): void {
    this.authService.signIn(FacebookLoginProvider.PROVIDER_ID);
  }

  signOut(): void {
    this.authService.signOut();
  }

  googleLogin(){
    this.loading = true;
    this.loginService.getGoogleConsentPageUrl()
                      .subscribe((resp:any) => {
                        if(resp.statusCode === 200){
                          this.googlereDirectUrl = resp.url;
                          window.location.href = this.googlereDirectUrl;
                        }
                        else{
                          alert('Failed : ' + resp.errorMessages);
                        }
                        this.loading = false;
                        },
                        (error:any) => {
                          alert('Something went wrong!');
                        });
  }

  registerCustomer(){
    this.registerLoding = true;
    this.loginService.createCustomer(this.fName, this.lName, this.emailId, rsaencrypt(this.password, this.tenantStore.publicKey), this.otp)
                      .subscribe((resp:any) => {
                        if(resp.statusCode === 200){
                          alert("sucess!");
                        }
                        else{
                          alert('Failed : ' + resp.errorMessages);
                        }
                        this.registerLoding = false;
                        },
                        (error:any) => {
                          alert('Something went wrong!');
                        });
  }

  login(){
    this.signInLoading = true;
    this.loginService.loginCustomer(this.emailId, rsaencrypt(this.password, this.tenantStore.publicKey), this.rememberMe)
                      .subscribe((resp:any) => {
                        if(resp.statusCode === 200){
                          this.userStore.JwtToken = resp.data.token;
                          this.userStore.active=resp.dataList[0].active;
                          this.userStore.emailId=resp.dataList[0].emailId;
                          this.userStore.customerAddress=resp.dataList[0].customerAddress;
                          this.userStore.userId=resp.dataList[0].customerId;
                          this.userStore.firstName=resp.dataList[0].firstName;
                          this.userStore.lastName=resp.dataList[0].lastName;
                          this.userStore.mobile=resp.dataList[0].mobile;
                          if(this.userStore.mobile === null || this.userStore.mobile === undefined){
                            this._snackBar.open('Please Update Mobile Number under Profile to Earn Loyality points!', '', this.commonService.alertoptionsWarn);
                          }
                          this.userStore.loyalityPoints=resp.dataList[0].loyalitypoint;
                          this.userStore.lastLogin=resp.dataList[0].lastLogin;
                          this.userStore.profilePic=resp.dataList[0].profilePic;
                          this.userStore.profilePicUrl=resp.dataList[0].profilePicUrl;
                          this.userStore.loginMode=resp.dataList[0].loginMode;

                          this.cookieService.deleteAll();
                          this.cookieService.set('CLIENTJWT', this.userStore.JwtToken, 90);

                          this.setCartItems();

                          this.route.navigate(["/home"]);
                        }
                        else{
                          alert('Failed : ' + resp.errorMessages);
                        }
                        this.signInLoading = false;
                        },
                        (error:any) => {
                          alert('Something went wrong!');
                        });
  }

  setCartItems(){
    this.cartService.getCartCount()
                    .subscribe((resp:any) => {
                      if(resp.statusCode === 200){
                        this.userStore.cartCount = resp.data;
                      }
                      else{
                        alert('Failed : ' + resp.errorMessages);
                      }
                      },
                      (error:any) => {
                        alert('Something went wrong!');
                      });
  }

  sendRegisterOTP(){
    this.buttonLoading=true;
    this.buttonDisable = true;
    if(this.emailId === undefined || this.emailId === null || this.emailId === ''){
      alert("Email is mandatory!")
      this.buttonLoading = false;
      this.buttonDisable = false;
      return;
    }
    this.loginService.sendRegisterOtp(this.emailId)
                      .subscribe((resp:any) => {
                          if(resp.statusCode !== 200){
                            alert('Failed : ' + resp.errorMessages);
                            this.buttonDisable = false;
                          }
                          this.buttonLoading = false;
                      },
                      (error:any) => {
                        alert('Something went wrong!');
                        this.buttonDisable = false;
                      });
  }

  forgotPassword(){
    const dialogRef = this.dialog.open(LoginDialogComponent, {
      width: 'auto',
      data: {

      }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }

  sendOtp(){
    if(this.mobile === undefined || this.mobile === null || this.mobile === ''){
      alert("Mobile Number is mandatory!")
      return;
    }
    this.registerLoding = true;
    this.loginService.sendRegisterMobileOtp(this.mobile)
                      .subscribe((resp:any) => {
                          if(resp.statusCode !== 200){
                            alert('Failed : ' + resp.errorMessages);
                          }
                          this.registerLoding = false;
                      },
                      (error:any) => {
                        alert('Something went wrong!');
                        this.registerLoding = false;
                      });
  }

  signupWithMobile(){
    if(this.mobile === undefined || this.mobile === null || this.mobile === ''){
      alert("Mobile Number is mandatory!")
      return;
    }
    if(this.otpnumber === undefined || this.otpnumber === null){
      alert("OTP Number is mandatory!")
      return;
    }
    this.registerLoding = true;
    this.loginService.createCustomerWithMobile(this.fName, this.lName, this.mobile, rsaencrypt(this.password, this.tenantStore.publicKey), this.otp)
                      .subscribe((resp:any) => {
                        if(resp.statusCode === 200){
                          alert("sucess!");
                        }
                        else{
                          alert('Failed : ' + resp.errorMessages);
                        }
                        this.registerLoding = false;
                        },
                        (error:any) => {
                          alert('Something went wrong!');
                        });
  }

}
