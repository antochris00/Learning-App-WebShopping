import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  updateEmployeeEndpoint = "/secure/admin/employee/updateEmployee"

  constructor(private http: HttpClient) { }

  updateEmployee(file, id, firstName, lastName, email, mobile): Observable<any>{
    const uploadData = new FormData();
    if(file=== undefined){
      file=null;
    }
    uploadData.append('myFile', file);
    uploadData.append('id', id);
    uploadData.append('firstName', firstName);
    uploadData.append('lastName', lastName);
    uploadData.append('emailId', email);
    uploadData.append('mobile', mobile);
    return this.http.put(environment.backendBaseUrl+this.updateEmployeeEndpoint, uploadData);
  }

}
