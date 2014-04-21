### RunscopeEverything

This is an [Xposed Module](http://repo.xposed.info/) that rewrites all HTTP
requests to point towards a Runscope bucket:

![](https://s3.amazonaws.com/pushbullet-uploads/ujv6ZOvSLKe-SO3Y0wOkHbO53fLqyoNRZ36XT3gWOXyI/Screenshot_2014-04-20-21-23-06.png)

Put in the Bucket ID (you can find it from the URL, or from the "URL Helper
section", an example is 'fnd4w6iq3qf2') and hit Save. All newly started
applications will send requests to that bucket. Clear the bucket ID in order
to disable RunscopeEverything.
