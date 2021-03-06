import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  dashboardReportEndpoint = "/secure/admin/dashboard/getDashboardReport";
  dashboardWeeklyReportEndpoint = "/secure/admin/dashboard/getDashboardWeeklyReport";

  constructor(private http: HttpClient) { }

  getDashboardReport(): Observable<any>{
    return this.http.get(environment.backendBaseUrl+this.dashboardReportEndpoint);
  }

  getDashboardweeklyReport(): Observable<any>{
    return this.http.get(environment.backendBaseUrl+this.dashboardWeeklyReportEndpoint);
  }

}
