module.factory('Paginator', function(){
	return function(fetchFunction, pageSize, topic){
		var paginator = {
				hasNextVar: false,
				fetch: function(page){
					this.currentOffset = (page - 1) * pageSize;
					this._load();
				},
				next: function(){
					if(this.hasNextVar){
						this.currentOffset += pageSize;
						this._load();
					}
				},
				_load: function(){
					var self = this;  //must use  self
					self.currentPage = Math.floor(self.currentOffset/pageSize) + 1;
					fetchFunction(this.currentOffset, pageSize + 1, topic, function(data){
						items = data.message;
						length = data.size;
						if(length == 0){
							return;
						}
						self.totalPage = Math.ceil(length/pageSize);
						self.endPage = self.totalPage;
						//生成链接
						if (self.currentPage > 1 && self.currentPage < self.totalPage) {
							self.pages = [
			                    self.currentPage - 1,
			                    self.currentPage,
			                    self.currentPage + 1
			                ];
			            } else if (self.currentPage == 1 && self.totalPage > 1) {
			            	self.pages = [
			                    self.currentPage,
			                    self.currentPage + 1
			                ];
			            } else if (self.currentPage == self.totalPage && self.totalPage > 1) {
			            	self.pages = [
			                    self.currentPage - 1,
			                    self.currentPage
			                ];
			            }
						self.currentPageItems = items.slice(0, pageSize);
						for(var i = 0; i < self.currentPageItems.length; ++i){
							if(self.currentPageItems[i].finished){
								self.currentPageItems[i].finished = "已导出";
							}else{
								self.currentPageItems[i].finished = "导出中";
							}
						}
						self.hasNextVar = items.length === pageSize + 1;
					});
				},
				hasNext: function(){
					return this.hasNextVar;
				},
				previous: function(){
					if(this.hasPrevious()){
						this.currentOffset -= pageSize;
						this._load();
					}
				},
				hasPrevious: function(){
					return this.currentOffset !== 0;
				},
				totalPage: 1,
				pages : [],
				lastpage : 0,
				currentPage: 1,
				endPage: 1,
				
				currentPageItems: [],
				currentOffset: 0
		};
		
		//加载第一页
		paginator._load();
		return paginator;
	};
});

module.controller('ConsumerIdSettingController', ['$rootScope', '$scope', '$http', 'Paginator', 'ngDialog','$interval',
                function($rootScope, $scope, $http, Paginator, ngDialog,$interval){
	var fetchFunction = function(offset, limit, name, callback){
		var transFn = function(data){
			return $.param(data);
		}
		var postConfig = {
				transformRequest: transFn
		};
		var data = {'offset' : offset,
								'limit': limit,
								'topic': name};
		$http.get(window.contextPath + $scope.suburl, {
			params : {
				offset : offset,
				limit : limit,
				topic: name
			}
		}).success(callback);
	};
	
	//for whitelist
	$http({
		method : 'GET',
		url : window.contextPath + '/console/topic/namelist'
	}).success(function(data, status, headers, config) {
		$('#topicprops').tagsinput({
			  typeahead: {
				  items: 16, 
				  source: data,
				  displayText: function(item){ return item;}  //necessary
			  }
		});
	}).error(function(data, status, headers, config) {
	});
	
	$scope.consumeridEntry = {};
	$scope.consumeridEntry.peak = 0;
	$scope.consumeridEntry.valley = 0;
	$scope.consumeridEntry.fluctuation = 0;
	$scope.consumeridEntry.whitelist = "";
	$scope.refreshpage = function(myForm){
		$('#myModal').modal('hide');
    	$scope.consumeridEntry.whitelist = $("#whitelist").val();
//    	$http.post(window.contextPath + '/console/setting/consumerid/create', {"peak":$scope.peak, 
//    		"valley":$scope.vallely, "fluctuation":$scope.fluctuation, "whitelist":$scope.whitelist})
//    		.success(function(response) {
//    			$scope.searchPaginator = Paginator(fetchFunction, $scope.adminnum, $scope.name , $scope.role);
//    	});
    	$http.post(window.contextPath + '/console/setting/consumerid/create', {"dto":$scope.consumeridEntry})
    		.success(function(response) {
    			$scope.searchPaginator = Paginator(fetchFunction, $scope.adminnum, $scope.name , $scope.role);
    		});
    }
	
	
	$scope.dialog = function(name) {
		$rootScope.removeName = name;
		ngDialog.open({
						template : '\
						<div class="widget-box">\
						<div class="widget-header">\
							<h4 class="widget-title">警告</h4>\
						</div>\
						<div class="widget-body">\
							<div class="widget-main">\
								<p class="alert alert-info">\
									您确认要删除吗？\
								</p>\
							</div>\
							<div class="modal-footer">\
								<button type="button" class="btn btn-default" ng-click="closeThisDialog()">取消</button>\
								<button type="button" class="btn btn-primary" ng-click="removefile(removeName)&&closeThisDialog()">确定</button>\
							</div>\
						</div>\
					</div>',
					plain : true,
					className : 'ngdialog-theme-default'
			});
	};
	
}]);

