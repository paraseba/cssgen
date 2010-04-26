cssgen
======

A clojure library to generate CSS code using an embedded domain-specific language (EDSL). In plain words: generate CSS files
writing clojure code.

Using clojure instead of plain CSS you get a lot of benefices. You can use the full power of the language to do things
like:

* Define constants and use them in your rules
* Operate with your constants using clojure expressions
* Define reusable CSS snippets using plain old clojure functions
* Define nested rules
* Generate readable styles with better code organization
* Easy CSS 'hacks'

Cssgen is like [sass](http://sass-lang.com/) but embedded in clojure. I love sass, but I hate the fact that it's not
embedded, that means that you have to re-learn a lot of constructions already present in the host language (Ruby in the
case of sass):

* sass mixin definitions are Ruby method definition
* sass mixin inlining is Ruby's function call
* sass `@import` is Ruby's `require`
* sass `@if` is Ruby's `if`, same thing for `@for` and `@while`
* SassScript is basically Ruby expressions.
* variables are Ruby variables
* etc.

cssgen takes a different approach, you already know all those constructions from clojure, so you use what you know.
cssgen gives you just the syntactic sugar needed to make generating CSS easy.

Installation
============

The easiest way to install cssgen is by using Leiningen. Just add the following dependency to your project.clj file:

  [cssgen "0.1.0"]

Usage
=====

I'll show some examples of use:

* CSS properties (we will have more syntactic sugar soon):
  
  (prop :border :none
        :color "#fff"
        :padding 0)

* You can nest calls to `prop` like in:

  (def nice-border
    (prop :border-style :solid
          :border-color "#98bf21"))

  (prop :background-color :white
        nice-border)

* CSS rules:

  (rule ".cell, .block"
    (prop :display :block)
    nice-border)

* You can nest rules:

  (rule ".cell, .block"
    (prop :display :block)
    nice-border

    (rule "a"   ; this will generate a rule for ".cell a, .block a"
      (prop :color :blue)))

* If you need the parent selector on the nested rule, you can use "&" and it will get replaced:

  (rule "a"
    (prop :color "#00C")
    (rule "&:hover"
      (prop :color "#0CC")))

* You can declare mixins with multiple rules and properties:

  (defn link-colors
    ([normal] (link-colors normal nil))
    ([normal hover] (link-colors normal hover nil))
    ([normal hover active] (link-colors normal hover active nil))
    ([normal hover active visited] (link-colors normal hover active visited nil))
    ([normal hover active visited focus]
      (mixin
        (prop :color normal)
        (if visited (rule "&:visited" (prop :color visited)))
        (if focus (rule "&:focus" (prop :color focus)))
        (if hover (rule "&:hover" (prop :color hover)))
        (if active (rule "&:active" (prop :color active))))))

* `mixin` is just a way to group properties and rules, you don't need it if you are returning just one rule or set of
properties.
    
* To generate a new CSS file from the current clj code do:
  
  (ns my-ns
    (:use cssgen))

  (css-file "public/css/screen.css"  ;this is the path of the target CSS file
    (rule ".hidden"
      (prop :display :none)))


ToDo
====

* More syntactic sugar
* Added methods to define rules without actually generating any CSS file
* Property namespaces (font-*) 
* Introduce dimensions and colors: (em 1), (px 20), (rgb 22 33 44)
* Write a sass "compiler" to migrate from sass to cssgen
* Use that compiler to generate the whole [compass](http://compass-style.org/) tree in cssgen.
* Document
* Command line interface
