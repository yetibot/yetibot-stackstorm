# yetibot-stackstorm

Add yetibot-stackstorm as a dependency to your yetibot installation and
configure it.

This is a yetibot plugin. For more information on yetibot and how to use
plugins, see [yetibot](https://github.com/devth/yetibot).

[![Clojars Project](http://clojars.org/yetibot-stackstorm/latest-version.svg)](http://clojars.org/yetibot-stackstorm)

## Config

```edn
;; merge this into config/config.edn in your yetibot installation
{:yetibot-stackstorm
 {:models
  {:stackstorm
   {:api-key ""
    :api-endpoint ""}}}}
```

## License

Copyright © 2015 Trevor C. Hartman

Distributed under the Eclipse Public License version 1.0.
