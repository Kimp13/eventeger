package main

import (
	"io/ioutil"
	"log"
	"net/http"

	graphql "github.com/graph-gophers/graphql-go"
	"github.com/graph-gophers/graphql-go/relay"
)

type RootResolver struct{}

func (*RootResolver) Info() (string, error) {
	return "Hello, world!", nil
}

func main() {
	data, err := ioutil.ReadFile("./schema.graphql")

	if err != nil {
		panic(err)
	}

	schema := graphql.MustParseSchema(string(data), &RootResolver{})

	http.Handle("/query", &relay.Handler{Schema: schema})
	log.Fatal(http.ListenAndServe(":8080", nil))
}
