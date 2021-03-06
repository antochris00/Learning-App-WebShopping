import { Component, OnInit } from '@angular/core';
import { AlertService } from '../../shared/_alert';
import { UserStoreService } from '../../service/userStore/user-store.service';
import { ProfileService } from '../../shared/profile/profile.service';
import { DomSanitizer } from '@angular/platform-browser';
import { TenantStoreService } from '../../service/tenantStore/tenant-store.service';
import { OrdersService } from '../../shared/orders/orders.service';
import { EmployeeService } from '../../shared/employee/employee.service';
declare var rsaencrypt: Function;

@Component({
  templateUrl: 'orders.component.html',
  styleUrls: ['orders.component.scss']
})
export class OrdersComponent implements OnInit {
  loading = false;
  pendingLoading = false;
  acceptedLoading = false;
  shippedLoading = false;
  alertoptions = {
    autoClose: true,
    keepAfterRouteChange: false
  };

  customerDetails: any;
  customerAddress: any;
  unassignedOrders: any[] = new Array();
  acceptedOrders: any[] = new Array();
  shippedOrders: any[] = new Array();
  productList: any[] = new Array();

  constructor(private orderService: OrdersService,
             private alertService: AlertService,
             private employeeService: EmployeeService){}

  ngOnInit(): void {
    this.refreshCards();
  }

  refreshCards(){
    this.getUnAssignedOrders();
    this.getAcceptedOrders();
    this.getShippedOrders();
  }

  changeStatus(orderId, status){
    this.loading = true;
    this.orderService.changeOrderStatus(status, orderId)
                      .subscribe((resp:any)=>{
                        if(resp.statusCode === 200){
                          this.alertService.success("Order Status Updated!", this.alertoptions);
                          this.refreshCards();
                        }
                        else{
                          this.alertService.error("Failed: "  + resp.errorMessages);
                        }
                        this.loading = false;
                      },
                      (error:any)=>{
                        this.alertService.error("Something Went Wrong!");
                        this.loading = false;
                      })
  }

  acceptOrder(orderId){
    this.changeStatus(orderId, "Accepted");
  }

  rejectOrder(orderId){
    this.changeStatus(orderId, "Cancelled");
  }

  outForDelivery(order: any){
    this.changeStatus(order.orderId, "OutForDelivery");
  }

  deliveredOrder(order: any){
    this.changeStatus(order.orderId, "delivered");
  }

  getUnAssignedOrders(){
    this.pendingLoading = true;
    this.orderService.getUnassignedOrders()
                     .subscribe((resp:any)=>{
                        if(resp.statusCode === 200){
                          this.unassignedOrders = resp.dataList;
                        }
                        else{
                          this.alertService.error("Failed: "  + resp.errorMessages);
                        }
                        this.pendingLoading = false;
                     },
                     (error:any)=>{
                       this.alertService.error("Something Went Wrong!");
                     })
  }

  getShippedOrders(){
    this.shippedLoading = true;
    this.orderService.getAssignedOrders("OutForDelivery")
                     .subscribe((resp:any)=>{
                        if(resp.statusCode === 200){
                          this.shippedOrders = resp.dataList;
                        }
                        else{
                          this.alertService.error("Failed: "  + resp.errorMessages);
                        }
                        this.shippedLoading = false;
                     },
                     (error:any)=>{
                       this.alertService.error("Something Went Wrong!");
                     })
  }

  getAcceptedOrders(){
    this.acceptedLoading = true;
    this.orderService.getAssignedOrders("Accepted")
                     .subscribe((resp:any)=>{
                        if(resp.statusCode === 200){
                          this.acceptedOrders = resp.dataList;
                        }
                        else{
                          this.alertService.error("Failed: "  + resp.errorMessages);
                        }
                        this.acceptedLoading = false;
                     },
                     (error:any)=>{
                       this.alertService.error("Something Went Wrong!");
                     })
  }

  setProductData(order:any){
    this.productList.length = 0;
    let data: any[] = order.orderDetails;
    data.forEach(element => {
      this.productList.push(element);
    })
  }

  getCustomerDetails(id, addressId){
    this.acceptedLoading = true;
    this.employeeService.getCustomerById(id)
                        .subscribe((resp:any)=>{
                          if(resp.statusCode === 200){
                            this.customerDetails = resp.data;
                            this.customerDetails.customerAddress.forEach(address => {
                              if(address.addressId === addressId){
                                this.customerAddress = address;
                              }
                            })
                          }
                          else{
                            this.alertService.error("Failed: "  + resp.errorMessages);
                          }
                          this.acceptedLoading = false;
                        },
                        (error:any)=>{
                          this.alertService.error("Something Went Wrong!");
                        })
  }

  getCustomerFirstName(){
    if(this.customerDetails !== undefined){
      return this.customerDetails.firstName;
    }
  }

  getCustomerLastName(){
    if(this.customerDetails !== undefined){
      return this.customerDetails.lastName;
    }
  }

  getCustomerMobile(){
    if(this.customerDetails !== undefined){
      return this.customerDetails.mobile;
    }
  }

  getCustomerDoorNumber(){
    if(this.customerAddress !== undefined){
      return this.customerAddress.doorNumber;
    }
  }

  getCustomerStreet(){
    if(this.customerAddress !== undefined){
      return this.customerAddress.street;
    }
  }

  getCustomerCity(){
    if(this.customerAddress !== undefined){
      return this.customerAddress.city;
    }
  }

  getCustomerPin(){
    if(this.customerAddress !== undefined){
      return this.customerAddress.pincode;
    }
  }

  getDeliveryContact(){
    if(this.customerAddress !== undefined){
      return this.customerAddress.mobileContact;
    }
  }

}
