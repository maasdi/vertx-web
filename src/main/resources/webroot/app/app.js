angular.module('VertxWeb', ['angular-storage', 'ui.router'])
    .constant('ENDPOINT_URI', '/api')
    .constant('BASE_URI', '')
    .config(function($stateProvider, $urlRouterProvider, $httpProvider) {
        $stateProvider
            .state('login', {
                url: '/login',
                templateUrl: 'app/templates/login.tmpl.html',
                controller: 'LoginCtrl',
                controllerAs: 'login'
            })
            .state('dashboard', {
                url: '/dashboard',
                templateUrl: 'app/templates/dashboard.tmpl.html',
                controller: 'DashboardCtrl',
                controllerAs: 'dashboard'
            })
            .state('users', {
                url: '/users',
                templateUrl: 'app/templates/userlist.tmpl.html',
                controller: 'UserListCtrl',
                controllerAs: 'userlist'
            })
            .state('userAdd', {
                url: '/users/new',
                templateUrl: 'app/templates/useradd.tmpl.html',
                controller: 'UserAddCtrl',
                controllerAs: 'useradd'
            })
            .state('userEdit', {
                url: '/users/:username',
                templateUrl: 'app/templates/useredit.tmpl.html',
                controller: 'UserEditCtrl',
                controllerAs: 'useredit'
            })
            .state('upload', {
                url: '/upload',
                templateUrl: 'app/templates/upload.tmpl.html',
                controller: 'UploadCtrl',
                controllerAs: 'upload'
            })
            .state('uploadview', {
                url: '/upload/:filename',
                templateUrl: 'app/templates/uploadview.tmpl.html',
                controller: 'UploadViewCtrl',
                controllerAs: 'uploadview'
            })
        ;

        $urlRouterProvider.otherwise('/dashboard');

        $httpProvider.interceptors.push('APIInterceptor');
    })
    .directive('fileModel', function ($parse) {
        return {
            restrict: 'A',
            link: function(scope, element, attrs) {
                console.log(attrs);
                var model = $parse(attrs.fileModel);
                var modelSetter = model.assign;

                element.bind('change', function(){
                    scope.$apply(function(){
                        modelSetter(scope, element[0].files[0]);
                    });
                });
            }
        };
    })
    .service('APIInterceptor', function($rootScope, UserContext) {
        var service = this;

        service.request = function(config) {
            var currentUser = UserContext.getCurrentUser(),
                access_token = currentUser ? currentUser.access_token : null;

            if (access_token) {
                config.headers.authorization = access_token;
            }
            return config;
        };

        service.responseError = function(response) {
            if (response.status === 401) {
                $rootScope.$broadcast('unauthorized');
            }
            return response;
        };
    })
    .service('UploadService', function ($http, ENDPOINT_URI) {
        var service = this;

        service.uploadFileToUrl = function(file){
            var fd = new FormData();
            fd.append('file', file);
            return $http.post(ENDPOINT_URI + '/upload', fd, {
                transformRequest: angular.identity,
                headers: {'Content-Type': undefined}
            });
        }
    })
    .service('UserContext', function(store) {
        var service = this,
            currentUser = null,
            userPermissions = [];

        service.setCurrentUser = function(user) {
            currentUser = user;
            store.set('user', user);
            return currentUser;
        };

        service.getCurrentUser = function() {
            if (!currentUser) {
                currentUser = store.get('user');
            }
            return currentUser;
        };

        service.setPermissions = function(permissions) {
            userPermissions = permissions;
            store.set('permissions', permissions);
            return userPermissions;
        };

        service.resetPermissions = function() {
            userPermissions = [];
        };

        service.isPermitted = function(permission) {
            if (!userPermissions) {
                userPermissions = store.get('permissions');
            }
            if (userPermissions.length > 0) {
                for (var i = 0; i  < userPermissions.length; i++) {
                    var entry = userPermissions[i];
                    if (permission === entry.perm) {
                        return true;
                    }
                }
            }
            return false;
        }

    })
    .service('UserService', function($http, ENDPOINT_URI) {
        var service = this;

        service.getAllUser = function() {
            return $http.get(ENDPOINT_URI + '/users');
        };

        service.addUser = function(user) {
            return $http.post(ENDPOINT_URI + '/users', user);
        };

        service.getUserById = function(username) {
            return $http.get(ENDPOINT_URI + '/users/' + username);
        };

        service.updateUser = function(user) {
            return $http.put(ENDPOINT_URI + '/users/' + user.username, user);
        };

        service.deleteUser = function(username) {
            return $http.delete(ENDPOINT_URI + '/users/' + username);
        };

        service.getUserPermissions = function() {
            return $http.post(ENDPOINT_URI + '/permission');
        }

    })
    .service('LoginService', function($http, BASE_URI) {
        var service = this;

        function getLogUrl(action) {
            return getUrl() + action;
        }

        service.login = function(credentials) {
            return $http.post(BASE_URI + '/login', credentials);
        };

        service.logout = function() {
            return $http.post(BASE_URI + '/logout');
        };

        service.register = function(user) {
            return $http.post(BASE_URI + '/register', user);
        };
    })
    .service('ItemsModel', function ($http, ENDPOINT_URI) {
        var service = this,
            path = '/items';

        service.all = function () {
            return $http.get(ENDPOINT_URI + path);
        };

        service.fetch = function (itemId) {
            return $http.get(ENDPOINT_URI + path + "/" + itemId);
        };

        service.create = function (item) {
            return $http.post(ENDPOINT_URI + path, item);
        };

        service.update = function (itemId, item) {
            return $http.put(ENDPOINT_URI + path + "/" + itemId, item);
        };

        service.destroy = function (itemId) {
            return $http.delete(ENDPOINT_URI + path + "/" + itemId);
        };
    })
    .controller('LoginCtrl', function($rootScope, $state, LoginService, UserContext){
        var login = this;

        function signIn(user) {
            LoginService.login(user)
                .then(function(response) {
                    if (response.status == 200) {
                        user.access_token = response.data.id;
                        UserContext.setCurrentUser(user);
                        $rootScope.$broadcast('authorized');
                        $state.go('dashboard');
                    } else {
                        login.message = 'Wrong username or password';
                    }
                });
        }

        function register(user) {
            LoginService.register(user)
                .then(function(response) {
                    login(user);
                });
        }

        function submit(user) {
            login.newUser ? register(user) : signIn(user);
        }

        login.newUser = false;
        login.submit = submit;
        login.message = null;
    })
    .controller('MainCtrl', function ($rootScope, $state, LoginService, UserService, UserContext) {
        var main = this;

        function initPermission() {
            UserService.getUserPermissions()
                .then(function(response) {
                    UserContext.setPermissions(response.data);
                }, function(error) {
                   console.log(error);
                });
        }

        function logout() {
            LoginService.logout()
                .then(function(response) {
                    main.currentUser = UserContext.setCurrentUser(null);
                    UserContext.resetPermissions();
                    $state.go('login');
                }, function(error) {
                    console.log(error);
                });
        }

        $rootScope.$on('authorized', function() {
            main.currentUser = UserContext.getCurrentUser();
            initPermission();
        });

        $rootScope.$on('unauthorized', function() {
            main.currentUser = UserContext.setCurrentUser(null);
            UserContext.resetPermissions();
            $state.go('login');
        });

        initPermission();

        main.logout = logout;
        main.currentUser = UserContext.getCurrentUser();
        main.isPermitted = function(name) {
            return UserContext.isPermitted(name);
        }
    })
    .controller('DashboardCtrl', function(ItemsModel){
        var dashboard = this;

        function getItems() {
            ItemsModel.all()
                .then(function (result) {
                    dashboard.items = result.data;
                });
        }

        function createItem(item) {
            ItemsModel.create(item)
                .then(function (result) {
                    initCreateForm();
                    getItems();
                });
        }

        function updateItem(item) {
            ItemsModel.update(item.id, item)
                .then(function (result) {
                    cancelEditing();
                    getItems();
                });
        }

        function deleteItem(itemId) {
            ItemsModel.destroy(itemId)
                .then(function (result) {
                    cancelEditing();
                    getItems();
                });
        }

        function initCreateForm() {
            dashboard.newItem = { name: '', description: '' };
        }

        function setEditedItem(item) {
            dashboard.editedItem = angular.copy(item);
            dashboard.isEditing = true;
        }

        function isCurrentItem(itemId) {
            return dashboard.editedItem !== null && dashboard.editedItem.id === itemId;
        }

        function cancelEditing() {
            dashboard.editedItem = null;
            dashboard.isEditing = false;
        }

        dashboard.items = [];
        dashboard.editedItem = null;
        dashboard.isEditing = false;
        dashboard.getItems = getItems;
        dashboard.createItem = createItem;
        dashboard.updateItem = updateItem;
        dashboard.deleteItem = deleteItem;
        dashboard.setEditedItem = setEditedItem;
        dashboard.isCurrentItem = isCurrentItem;
        dashboard.cancelEditing = cancelEditing;

        initCreateForm();
        getItems();
    })
    .controller('UserListCtrl', function($scope, UserService) {

        function getAll() {
            UserService.getAllUser()
                .then(function(result) {
                    $scope.users = result.data;
                }, function(error) {
                    console.log(error);
                });
        }

        getAll();
    })
    .controller('UserAddCtrl', function($state, $scope, UserService) {
         $scope.master = {};

         $scope.addUser = function(user) {
            UserService.addUser(user)
                .then(function(response) {
                    $state.go('users');
                }, function(error) {
                   console.log(error);
                });

            $scope.reset = function() {
                $scope.user = angular.copy($scope.master);
            };

            $scope.reset();
        };

    })
    .controller('UserEditCtrl', function($state, $scope, $stateParams, UserService) {

        function getById(username) {
            UserService.getUserById(username)
                .then(function(response) {
                    $scope.user = response.data;
                }, function(error) {
                    console.log(error);
                });
        };

        $scope.update = function(user) {
                UserService.updateUser(user)
                    .then(function (response) {
                        $state.go('users');
                    }, function (error) {
                        console.log(error);
                    });
        };

        $scope.delete = function(user) {
            var deleted = confirm('Are you absolutely sure you want to delete?');
            if (deleted) {
                UserService.deleteUser(user.username)
                    .then(function(response) {
                        $state.go('users');
                    }, function(error) {
                       console.log(error);
                    });
            }
        };

        getById($stateParams.username);

    })
    .controller('UploadCtrl', function($state, $scope, UploadService) {
        $scope.myFile = null;
        $scope.filename = null;

        $scope.uploadFile = function() {
            console.log('Start to upload..');
            var file = $scope.myFile;
            UploadService.uploadFileToUrl(file)
                .then(function(response) {
                    $state.go('uploadview', {filename : response.data.filename});
                }, function(error) {
                   console.log(error);
                });
        };
    })
    .controller('UploadViewCtrl', function($scope, $stateParams) {
        $scope.filename = $stateParams.filename;
    })
;
