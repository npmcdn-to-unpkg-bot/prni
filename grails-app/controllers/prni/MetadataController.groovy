package prni

import org.grails.io.support.GrailsResourceUtils

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class MetadataController {

    def grailsResourceLocator

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 20, 100)
        respond Metadata.list(params), model: [metadataCount: Metadata.count()]
    }

    def iso(Integer id){
        def m = Metadata.get(id)
        String uuid = m.geonetwork.trim()
        response.setContentType("text/xml")
        String path =request.getSession().getServletContext().getRealPath("meta") + "/" + uuid + ".xml"

        String xml = new File(path).getText()
        render xml
    }

    def listCountry(String countryCode) {
        int max = 999
        params.max = Math.min(max ?: 10, 100)
        def country = Country.findByCode(countryCode)
        def list = Metadata.findAllByCountry(country, params)
        respond list, model: [metadataCount: list.size(), country: country]
    }


    def show(Metadata metadata) {
        respond metadata
    }

    def create() {
        respond new Metadata(params)
    }

    @Transactional
    def save(Metadata metadata) {
        if (metadata == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (metadata.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond metadata.errors, view: 'create'
            return
        }

        metadata.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'metadata.label', default: 'Metadata'), metadata.id])
                redirect metadata
            }
            '*' { respond metadata, [status: CREATED] }
        }
    }

    def edit(Metadata metadata) {
        respond metadata
    }

    @Transactional
    def update(Metadata metadata) {
        if (metadata == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (metadata.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond metadata.errors, view: 'edit'
            return
        }

        metadata.save flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'metadata.label', default: 'Metadata'), metadata.id])
                redirect metadata
            }
            '*' { respond metadata, [status: OK] }
        }
    }

    @Transactional
    def delete(Metadata metadata) {

        if (metadata == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        metadata.delete flush: true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'metadata.label', default: 'Metadata'), metadata.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'metadata.label', default: 'Metadata'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

}
