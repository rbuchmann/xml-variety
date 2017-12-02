package main

import (
	"fmt"
	"strings"
)

type stream struct {
	input string
	start int
	pos   int
}

type node struct {
	tag      string
	attrs    map[string]string
	children []interface{}
}

func peek(s *stream) byte {
	return s.input[s.pos]
}

func isDone(s *stream) bool {
	return s.pos >= len(s.input)
}

func slice(s *stream) string {
	res := string(s.input[s.start:s.pos])
	s.start = s.pos
	return res
}

func skipWhitespace(s *stream) {
	for !isDone(s) && string(peek(s)) == " " {
		s.pos++
		s.start++
	}
}

func startsWith(s *stream, prefix string) bool {
	return strings.HasPrefix(string(s.input[s.pos:]), prefix)
}

func skip(s *stream, prefix string) bool {
	if startsWith(s, prefix) {
		s.pos += len(prefix)
		s.start += len(prefix)
		return true
	}
	return false
}

func assertNext(s *stream, c string) {
	if nxt := string(peek(s)); nxt != c {
		panic(fmt.Sprintf("Oh noes, expected %v but got %v", c, nxt))
	}
	s.pos++
	s.start++
}

func scanTag(s *stream) string {
	for {
		if isDone(s) || strings.IndexByte(">/ ", peek(s)) >= 0 {
			return slice(s)
		}
		s.pos++
	}
}

func scanAttr(s *stream) (string, string) {
	for !isDone(s) && strings.IndexByte(" =", peek(s)) < 0 {
		s.pos++
	}
	key := slice(s)

	skipWhitespace(s)
	assertNext(s, "=")
	skipWhitespace(s)

	assertNext(s, "\"")
	for !isDone(s) && string(peek(s)) != "\"" {
		s.pos++
	}
	value := slice(s)
	assertNext(s, "\"")
	return key, value
}

func scanAttrs(s *stream) map[string]string {
	result := make(map[string]string)
	for {
		skipWhitespace(s)
		if isDone(s) || strings.IndexByte(">/", peek(s)) >= 0 {
			return result
		}
		k, v := scanAttr(s)
		result[k] = v
	}
}

func scanIsSelfClosing(s *stream) bool {
	skipWhitespace(s)
	if skip(s, "/>") {
		return true
	}
	assertNext(s, ">")
	return false
}

func scanText(s *stream) string {
	for !isDone(s) && string(peek(s)) != "<" {
		s.pos++
	}
	return slice(s)
}

func assertMatchingCloseTag(s *stream, tag string) {
	assertNext(s, "<")
	assertNext(s, "/")
	skipWhitespace(s)
	closeTag := scanTag(s)
	if closeTag != tag {
		panic(fmt.Sprintf("Expected tag %v but got %v", tag, closeTag))
	}
	skipWhitespace(s)
	assertNext(s, ">")
}

func scanNode(s *stream) node {
	assertNext(s, "<")
	tag := scanTag(s)
	attrs := scanAttrs(s)
	var children []interface{}
	if done := scanIsSelfClosing(s); !done {
		skipWhitespace(s)
		for !startsWith(s, "</") {
			if string(peek(s)) == "<" {
				childNode := scanNode(s)
				children = append(children, childNode)
			} else {
				childTextNode := scanText(s)
				children = append(children, childTextNode)
			}
			skipWhitespace(s)
		}
		assertMatchingCloseTag(s, tag)
	}
	return node{tag, attrs, children}
}

func formatAttrs(attrs map[string]string) string {
	result := []string{}
	for k, v := range attrs {
		result = append(result, fmt.Sprintf(":%v \"%v\"", k, v))
	}
	return fmt.Sprintf("{%v}", strings.Join(result, ","))
}

func formatChildren(children []interface{}) []string {
	formattedChildren := []string{}
	for _, child := range children {
		switch v := child.(type) {
		case node:
			formattedChildren = append(formattedChildren, formatNode(v))
		case string:
			formattedChildren = append(formattedChildren, fmt.Sprintf("\"%v\"", v))
		}
	}
	return formattedChildren
}

func formatNode(n node) string {
	return fmt.Sprintf("[:%v %v %v]", n.tag, formatAttrs(n.attrs),
		strings.Join(formatChildren(n.children), " "))
}

const test = "<foo bar=\"baz\">qux<a href=\"blah\">blubs</a></foo>"

func main() {
	s := &stream{input: test}
	n := scanNode(s)
	fmt.Println(formatNode(n))
}
