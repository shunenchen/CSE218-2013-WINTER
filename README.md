Hockey Game Manager
===================

Developing
----------

Like most Clojure projects, we are using [Leiningen]. Install it and
then run 

    $ lein run
    
to get a local server going, or 

    $ lein repl

to start the repl.

[Leiningen]: http://leiningen.org/

Deploying
---------

Our application is hosted on [Heroku], you will need their [toolbelt]
in order to deploy updates. Once you have it installed run

    $ git remote add heroku git@heroku.com:hockey-game-manager.git
    
to add the production remote, and

    $ git push heroku master

to deploy. You'll need to give Eric the email address associated with
your Heroku account in order to deploy.

[Heroku]: http://heroku.com
[toolbelt]: http://toolbelt.heroku.com/
