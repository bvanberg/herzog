var Path = require('path');

// UI Endpoints
exports.endpoints = [
    {method: 'GET', path: '/',                handler: function (request, reply) {reply.file(Path.join(__dirname, '/app/index.html'));}},
    {method: 'GET', path: '/{filename}',      handler: {directory: {path: Path.join(__dirname, '/app')}}},
    {method: 'GET', path: '/assets/{param*}', handler: {directory: {path: Path.join(__dirname, '/app/assets')}}},
    {method: 'GET', path: '/vendor/{param*}', handler: {directory: {path: Path.join(__dirname, '/app/vendor')}}},
    {method: 'GET', path: '/views/{param*}',  handler: {directory: {path: Path.join(__dirname, '/app/views')}}}
    //{method: 'GET', path: '/{sessionId}/login', handler: function (request, reply) {
    //    reply('Login: ' + JSON.stringify(request));
    //}},

];