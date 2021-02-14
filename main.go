package main

import (
	"io/ioutil"
	"log"
	"net/http"

	graphql "github.com/graph-gophers/graphql-go"
	"github.com/graph-gophers/graphql-go/relay"
)

type query struct{}

func (_ *query) Hello() string { return "Hello, world!" }

func main() {
	data, err := ioutil.ReadFile("./schema.graphql")

	if err != nil {
		log.Fatal("Provide GraphQL schema!")
		return
	}

	schema := graphql.MustParseSchema(string(data), &query{})
	http.Handle("/query", &relay.Handler{Schema: schema})
	log.Fatal(http.ListenAndServe(":8080", nil))
}
