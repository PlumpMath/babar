(ns babar.speech-acts-test
  (:require [midje.sweet :refer :all]
            [babar.parser :refer :all]
            [babar.speech-acts :refer :all]))

(defn reset-commitments []
  (reset! commitments {}))

(defn setup-commitments []
  (swap! commitments merge
         {:test (make-commitment (fn [] "test") 1 "completed" "error")}))

(facts "about parsing commitments"
  (= babar.speech_acts.Commitment (type (parse "*raise-temp"))) => true
  (against-background  (before :facts
                               (swap! commitments merge
                                      {:raise-temp (make-commitment '(+ 1 1) 1 true nil)}))))


(facts "about accepting requests"
  (type (parse "accept.request *up-temp fn [x] (+ x 1)")) => babar.speech_acts.Commitment
  (nil? (:up-temp @commitments)) => false)


(facts "about answering queries"
  (parse "answer.query request.value *test") => 1
  (parse "answer.query request.completed *test") => "completed"
  (nil? (parse "answer.query request.created *test")) => false
  (nil? (parse "answer.query request.fn *test")) => false
  (parse "answer.query request.errors *test") => "error"
  (against-background (before :facts (setup-commitments))))

(facts "about processing commitments"
  (type (parse "accept.request *dog fn [] :bark")) => babar.speech_acts.Commitment
  (parse "answer.query request.value *dog") => :bark
  (nil? (parse "answer.query request.completed *dog")) => false
  (against-background (before :facts (reset-commitments))))

(facts "about processing multiple commitments"
  (type (parse "accept.request *cat fn [] :meow")) => babar.speech_acts.Commitment
  (type (parse "accept.request *bird fn [] :tweet")) => babar.speech_acts.Commitment
  (type (parse "accept.request *horse fn [] :neigh")) => babar.speech_acts.Commitment
  (parse "answer.query request.value *cat") => :meow
  (nil? (parse "answer.query request.completed *cat")) => false
  (parse "answer.query request.value *bird") => :tweet
  (nil? (parse "answer.query request.completed *bird")) => false
  (parse "answer.query request.value *horse") => :neigh
  (nil? (parse "answer.query request.completed *horse")) => false
  (against-background (before :facts (reset-commitments))))

(facts "about processing commitment with an error"
  (type (parse "accept.request *cat fn [] / 0 0")) => babar.speech_acts.Commitment
  (parse "answer.query request.completed *cat") => nil
  (parse "answer.query request.value *cat") => nil
  (parse "answer.query request.errors *cat") => "Divide by zero"
  (against-background (before :facts (reset-commitments))))