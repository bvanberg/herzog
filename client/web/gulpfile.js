var gulp = require("gulp");
var zip = require("gulp-zip");
var Promise = require('bluebird');
var RequestP = Promise.promisifyAll(require('request'));
var Boom = require('boom');
path = require('path');
gulp.task('default', function() {});