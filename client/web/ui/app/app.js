'use strict';

// create modules for app (shortcut for: angular.module('App'))
var App = angular.module('App', [
  'ui.bootstrap',
  'ui.router'
]);

// routes
App.config(function($stateProvider, $urlRouterProvider) {

  $stateProvider

  // default states
  .state('app',{
    url: '/',
    views: {
      'header': {
        templateUrl: 'views/modules/header/header.html'
      },
      'footer': {
        templateUrl: 'views/modules/footer/footer.html'
      },
      'content': {
        templateUrl: 'views/pages/surf/surf.html'
      }
    }
  })

  //catch all route
  $urlRouterProvider.otherwise('/');

}); // App.config