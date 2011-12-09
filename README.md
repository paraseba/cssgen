# cssgen

A **clojure** library to generate **CSS** code using an embedded domain-specific
language (EDSL). In plain words: generate CSS files by writing clojure code.

You can go [here](http://wiki.github.com/paraseba/cssgen/) to read more about
cssgen.

Using clojure instead of plain CSS you get a lot of benefices. You can use the
full power of the language to do things like:

* Define constants and use them in your rules

    ```clojure
    (def width (px 960))

* Operate with your constants using clojure expressions

    ```clojure
    (def main-width (* width 0.6))

* Define nested rules

    ```clojure
    [:a.plain
      :color :inherit
      :text-decoration :inherit
      :cursor :inherit

      ["&:active, &:focus"
        :outline :none]]

* Define reusable CSS snippets using plain old clojure functions and vars

    ```clojure
    (def ^{:doc "ie hack"}
      has-layout
      (list :display :inline-block
        ["&" :display :block]))

    (def clearfix
      (list :overflow :hidden has-layout))

* Generate readable styles with better code organization

    ```clojure
    [:#nav
      (horizontal-list (px 9))
      [:a
        (link-colors my-link-color my-visited-color my-hover-color)]]

* Easy CSS 'hacks'

    ```clojure
    (defn- float-side [side]
      (list :display :inline :float side))

    (def ^{:doc "Implementation of float:left with fix for double-margin bug"}
      float-left
      (float-side :left))

    (def ^{:doc "Implementation of float:right with fix for double-margin bug"}
      float-right
      (float-side :right))

    [:#secondary
      float-right]


## Installation

The easiest way to install cssgen is by using Leiningen. Just add the following
dependency to your project.clj file:

    ```clojure
    [cssgen "0.3.0-SNAPSHOT"]

## Usage

I'll show some examples of use, but you should read the
[wiki](http://wiki.github.com/paraseba/cssgen/) for more details and information.

* CSS rules: to create a simple rule you use a clojure vector with the
selector as first element, and a series of property pairs.

    ```clojure
    ["ul.nav, ol"
      :color :black
      :background-color :#ddd
      :padding [:1px "2px" (px 3) 0]]

* Selectors and property values could be
** keywords, strings, numbers
** vectors and lists
** special constructions like <code>(px 9)</code>, <code>(% 30)</code>,
<code>(col :#aaa)</code> (more about this later)
** other types converted using <code>str</code> function.

If a property key must be associated with several values, you use a sequence of
values, like in the padding property above. Of course, if everything is
"literal", you could simply do <code>:padding "1px 2px 3px 4px"</code>.

* You can nest rules:

    ```clojure
    ["#main, #secondary"
      :padding "10px"

      [:h1   ; this will generate a rule for "#main h1, #secondary h1"
        :color :blue]]

to generate the following CSS

    ```css
    #main, #secondary {
      padding: 10px;
    }
      #main h1, #secondary h1 {
        color: blue;
      }

* If you need the parent selector on the nested rule, you can use "&" and it
will get replaced:

    ```clojure
    [:a
      :color "#00C"

      ["&:hover"  ; this will generate a rule for a:hover
        :color "#0CC"]]

will generate

    ```css
    a {
      color: #00C;
    }
      a:hover {
        color: #0CC;
      }


* You can define mixins with multiple rules and properties using functions (or
vars) that return sequences. Those can later be used in other definitions

    ```clojure
    (defn link-colors
      ([normal] (link-colors normal nil))
      ([normal hover] (link-colors normal hover nil))
      ([normal hover visited]
        (list
          :color normal
          (when visited ["&:visited" :color visited])
          (when hover   ["&:hover" :color hover]))))

    [:a (link-colors "#00c" "#0cc")]

* As you can see in the previous example, nils in the properties list will be ignored:

* You can easily define constants

    ```clojure
    (def width (px 960))
    (def h1-font-size (em 1.5))
    (def h1-color (rgb :#0000ca))
    (def h2-color (rgb :#0dd))
    (def form-size (% 60))

* And use the basic arithmetic operations on them

    ```clojure
    (def main-width (* 0.7 width))
    (def h2-font-size (- h1-font-size (em 0.3)))
    (def h4-color (/ (+ h1-color h2-color) 2))

* To generate a new CSS file from the current clj code do:


    ```clojure
    (ns killerapp.css.screen
      (:refer-clojure :exclude [+ - * /])
      (:use cssgen clojure.algo.generic.arithmetic))


    (spit "public/css/screen.css"  ;this is the path to the target CSS file
      (css
        .......
        .......  ; all your rules
        .......))

## Changes
* 0.3.0-SNAPSHOT
** Dropped <code>rule</code> and <code>mixin</code> functions in favor of vectors and lists
** Dropped <code>css-ns</code>, you're on your own now
** Dropped <code>css-file</code>, you're on your own now
** The only way to generate colors is using <code>rgb</code> function

## ToDo

* Watcher to re-generate css files if source changed ???
* Property namespaces (font-*)
* Write a sass "compiler" to migrate from sass to cssgen
* Use that compiler to generate the whole [compass](http://compass-style.org/)
tree in cssgen.
* Document
* Command line interface

h3. Check the [wiki](http://wiki.github.com/paraseba/cssgen/ for more usage)
information.

If you have a feature request, problem or comment, just drop me a line.
