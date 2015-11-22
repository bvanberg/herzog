var ApiRoutes = require('./api/routes');
var UiRoutes = require('./ui/routes');

// Data web server
// set up ======================================================================
var Hapi  = require('hapi');
var Path = require('path');

var server = new Hapi.Server(
    {
        //debug:{
        //    request:["received"]
        //}
        //app:{
        //    ;
        //}
    }
);

// configuration ===============================================================
server.connection({
    host: process.env.HOST || 'localhost',
    port: process.env.PORT || 8081,
    routes: { cors: true }
});

server.route(ApiRoutes.endpoints);
server.route(UiRoutes.endpoints);

//lout is a API documentation generator for hapi. Just go to http://<this app>/docs
server.register({ register: require('lout')}, function(err) {
    if (err) {
        console.log('louterror occurred: ' + err);
    }
});

// listen (start app with node server.js) ======================================
server.start(function() {
    console.log('Environment: ' + process.env.NODE_ENV);
    console.log("Hapi server started @ " + server.info.uri);
});
