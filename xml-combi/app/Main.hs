{-# LANGUAGE FlexibleContexts #-}

module Main where

import Data.List

import Text.Parsec
import Text.Parsec.String

type AttrName = String
type AttrVal  = String

data Attribute = Attribute (AttrName, AttrVal) deriving (Show)

data XML =  Element String [Attribute] [XML]
          | SelfClosingTag String [Attribute]
          | Decl String
          | Body String
        deriving (Show)

-- Parser

document = do
  spaces                     -- strip leading space
  y <- (try xmlDecl <|> tag) -- we may start with an XML declaration or a tag
  spaces
  x <- many tag
  spaces
  return (y : x)


xmlDecl ::Parser XML
xmlDecl = do
  string "<?xml"
  decl <- many (noneOf "?>")
  string "?>"
  return (Decl decl)

tag = do
  char '<'
  spaces
  name <- many (letter <|> digit)
  spaces
  attr <- many attribute
  spaces
  close <- try (string "/>" <|> string ">")
  if (length close) == 2
  then return (SelfClosingTag name attr)
  else do
        elementBody <- many elementBody
        endTag name
        spaces
        return (Element name attr elementBody)

-- The body of an element, consumes any leading spaces; would be nice to not have the try here
elementBody = spaces *> try tag <|> text

-- End tag, assuming that we had a normal, non self-closing tag
endTag str = string "</" *> string str <* char '>'

-- Create a body XML element, from text up to the next tag
text = Body <$> many1 (noneOf "><")

attribute = do
  name <- many (noneOf "= />")
  spaces
  char '='
  spaces
  char '"'
  value <- many (noneOf ['"'])
  char '"'
  spaces
  return (Attribute (name,value))

  -- Emitter

formatAttr (Attribute (name, val)) = ":" ++ name ++ " \"" ++ val ++ "\""

formatAttrs attrs = "{" ++ intercalate " " (map formatAttr attrs) ++ "}"

-- data XML =  Element String [Attribute] [XML]
--           | SelfClosingTag String [Attribute]
--           | Decl String
--           | Body String
--         deriving (Show)


formatDocument doc = case doc of
  Element tag attrs children -> "[:" ++ tag ++ " " ++ (formatAttrs attrs) ++ " " ++ intercalate "" (map formatDocument children) ++ "]"
  SelfClosingTag tag attrs -> formatDocument $ Element tag attrs []
  Decl decl -> decl
  Body txt -> txt

example = "<foo bar=\"baz\">bar</foo>"

main :: IO ()
main = case (parse document "" example) of
  Left err-> print err
  Right doc -> putStr $ intercalate "\n" $ map formatDocument doc
