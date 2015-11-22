'use strict';

// Load modules
var Config = require('config');
var settingsConfig = Config.get('surfHandler');

// Declare internals
var internals = {};


// Public function exports

exports.get = {
    validate: {
    },
    handler: function(request, reply) {
        reply();
    }
};
