-- :name get-sub-lines :? :*
-- :doc Get sub lines given a line id and sub id
SELECT *
FROM sub_link_lines
WHERE sub_link_id = :id
AND lineid >= :lineid
ORDER BY lineid
LIMIT :linecount

-- :name get-lines-in :? :*
-- :doc Get the line entries for the from lang
SELECT *
FROM sub_lines
WHERE id = :id
AND lineid IN (:v*:lineids)

-- :name get-sub-title :? :1
-- :doc Get the title for the subtitle
SELECT title
FROM sub_titles st,
     sub_links sl
WHERE sl.id = :id
AND st.id = sl.fromid

-- :name get-next-line-id :? :*
-- :doc Gets the next lineid after the input id
SELECT lineid
FROM sub_link_lines
WHERE lineid > :lineid
AND sub_link_id = :id
ORDER BY lineid
LIMIT :linecount

-- :name get-first-line-id :? :1
-- :doc Gets the first line id for a subtitle
SELECT lineid
FROM sub_link_lines
WHERE sub_link_id = :id
ORDER BY lineid
LIMIT 1

-- :name get-random-sub-id :? :1
-- :doc Returns a random subtitle id for a give pair of languages
select id
from (select links.id
      from languages as fromlang,
           languages as tolang,
           sub_links links
      where links.fromlang = fromlang.id
      and links.tolang = tolang.id
      and fromlang.name = lower(:fromlang)
      and tolang.name = lower(:tolang)
      union
      select links.id from languages as fromlang,
      languages as tolang,
      sub_links links
      where links.fromlang = fromlang.id
      and links.tolang = tolang.id
      and fromlang.name = lower(:tolang)
      and tolang.name = lower(:fromlang)) as langjoin
order by random() limit 1

-- :name get-language-pairs :? :*
-- :doc Given an input language, return all languages link to it
SELECT rightlang.name, rightlang.display_name
FROM language_pairs pair, languages leftlang, languages rightlang
WHERE pair.lang1 = leftlang.id
AND pair.lang2 = rightlang.id
AND leftlang.name = :lang
ORDER BY rightlang.name

-- :name get-sub-link-entry :? :1
-- :doc Get details for a sublink id
SELECT *
FROM sub_links
WHERE id = :id

-- :name get-language-by-id :? :1
-- :doc Get the language entry for a name
SELECT *
FROM languages
WHERE id = :id

-- :name get-language-by-name :? :1
-- :doc Return the language entry when queried by name
SELECT *
FROM languages
WHERE name = :name


-- :name add-user! :! :n
-- :doc Add a new user
INSERT INTO USERS (email, password)
VALUES (:email, :password)

-- :name get-user :? :1
-- :doc Get the details for a user
SELECT id, email, password
FROM users
WHERE email = :email

-- :name save-review! :! :n
-- :doc Save a user review
INSERT INTO reviews (userid, id, lineid, rev)
VALUES (:userid, :id, :lineid, :rev)

-- :name get-first-review :? :1
-- :doc Get the first review item for a user
SELECT *
FROM reviews
WHERE userid = :userid
ORDER BY review_time ASC
LIMIT 1

-- :name update-review! :! :n
-- :doc Update the review_time on a review
UPDATE reviews
SET review_time = current_timestamp
WHERE userid = :userid
AND id = :id
AND lineid = :lineid

-- :name delete-review! :! :n
-- :doc delete a review for a user
DELETE FROM reviews
WHERE userid = :userid
AND id = :id
AND lineid = :lineid

-- :name create-message! :! :n
-- :doc Add a user message
INSERT INTO messages(email, subject, body)
VALUES (:email, :subject, :body)

-- :name get-titles :? :*
-- :doc Returns titles that begin with letter
SELECT sl.id, st.title
FROM sub_titles st,
sub_links sl
WHERE st.title like :letter || '%'
AND st.id = sl.fromid
AND sl.fromlang = :fromlangid
AND sl.tolang = :tolangid
ORDER BY st.title

-- :name search-titles :? :*
-- :doc Search for a particular title
SELECT sl.id, st.title
FROM sub_titles st,
sub_links sl
WHERE lower(st.title) like '%' || lower(:search) || '%'
AND st.id = sl.fromid
AND sl.fromlang = :fromlangid
AND sl.tolang = :tolangid
ORDER BY st.title
LIMIT 1000





